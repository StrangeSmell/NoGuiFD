package com.strangesmell.noguifd;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.block.CookingPotBlock;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;
import vectorwing.farmersdelight.common.block.entity.inventory.CookingPotItemHandler;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.mixin.accessor.RecipeManagerAccessor;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModParticleTypes;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.utility.ItemUtils;
import vectorwing.farmersdelight.common.utility.TextUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class NGCookingPotBlockEntity extends SyncedBlockEntity implements MenuProvider, HeatableBlockEntity, Nameable, RecipeHolder{
    public Map<Integer, Float> rollCountX = new HashMap<Integer, Float>();
    public Map<Integer, Float> rollCountY = new HashMap<Integer, Float>();
    public Map<Integer, Float> rollCountZ = new HashMap<Integer, Float>();
    public Random randomRollX = new Random(1);
    public Random randomRollY = new Random(89846);
    public Random randomRollZ = new Random(1653165);
    public Random randomVecX = new Random(45161651);
    public Random randomVecY = new Random(23516);
    public Random randomVecZ = new Random(351);


    public Map<Integer, Float> indexCount = new HashMap<Integer, Float>();
    public final float maxIndexCount = 30;
    public void increaseIndexCount(int index){
        if(indexCount.get(index)>=maxIndexCount) return;
        indexCount.put(index, indexCount.get(index)+1);
    }
    public void decreaseIndexCount(int index){
        if (indexCount.get(index)<=0) return;
        indexCount.put(index, indexCount.get(index)-1);
    }

    public static final Map<Item, Item> INGREDIENT_REMAINDER_OVERRIDES;
    private final ItemStackHandler inventory = this.createHandler();
    private final LazyOptional<IItemHandler> inputHandler = LazyOptional.of(() -> {
        return new CookingPotItemHandler(this.inventory, Direction.UP);
    });
    private final LazyOptional<IItemHandler> outputHandler = LazyOptional.of(() -> {
        return new CookingPotItemHandler(this.inventory, Direction.DOWN);
    });
    private int cookTime;
    private int cookTimeTotal;
    private ItemStack mealContainerStack;
    private Component customName;
    protected final ContainerData cookingPotData;
    private final Object2IntOpenHashMap<ResourceLocation> usedRecipeTracker;
    private ResourceLocation lastRecipeID;
    private boolean checkNewRecipe;

    public NGCookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(NoGuiFD.NGCookingPotEntity.get(),pos, state);
        for(int index = 0; index < 9 ; index++){
            indexCount.put(index,0f);
        }
        for(int index = 0; index < 9 ; index++){
            rollCountX.put(index,0f);
        }
        for(int index = 0; index < 9 ; index++){
            rollCountY.put(index,0f);
        }
        for(int index = 0; index < 9 ; index++){
            rollCountZ.put(index,0f);
        }

        this.mealContainerStack = ItemStack.EMPTY;
        this.cookingPotData = this.createIntArray();
        this.usedRecipeTracker = new Object2IntOpenHashMap();
        this.checkNewRecipe = true;
    }

    public void load(CompoundTag compound) {
        super.load(compound);
        this.inventory.deserializeNBT(compound.getCompound("Inventory"));
        this.cookTime = compound.getInt("CookTime");
        this.cookTimeTotal = compound.getInt("CookTimeTotal");
        this.mealContainerStack = ItemStack.of(compound.getCompound("Container"));
        if (compound.contains("CustomName", 8)) {
            this.customName = Component.Serializer.fromJson(compound.getString("CustomName"));
        }

        CompoundTag compoundRecipes = compound.getCompound("RecipesUsed");
        Iterator var3 = compoundRecipes.getAllKeys().iterator();

        while(var3.hasNext()) {
            String key = (String)var3.next();
            this.usedRecipeTracker.put(new ResourceLocation(key), compoundRecipes.getInt(key));
        }

    }

    public void inventoryChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("CookTime", this.cookTime);
        compound.putInt("CookTimeTotal", this.cookTimeTotal);
        compound.put("Container", this.mealContainerStack.serializeNBT());
        if (this.customName != null) {
            compound.putString("CustomName", Component.Serializer.toJson(this.customName));
        }

        compound.put("Inventory", this.inventory.serializeNBT());
        CompoundTag compoundRecipes = new CompoundTag();
        this.usedRecipeTracker.forEach((recipeId, craftedAmount) -> {
            compoundRecipes.putInt(recipeId.toString(), craftedAmount);
        });
        compound.put("RecipesUsed", compoundRecipes);
    }

    private CompoundTag writeItems(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("Container", this.mealContainerStack.serializeNBT());
        compound.put("Inventory", this.inventory.serializeNBT());
        return compound;
    }

    public CompoundTag writeMeal(CompoundTag compound) {
        if (this.getMeal().isEmpty()) {
            return compound;
        } else {
            ItemStackHandler drops = new ItemStackHandler(9);

            for(int i = 0; i < 9; ++i) {
                drops.setStackInSlot(i, i == 6 ? this.inventory.getStackInSlot(i) : ItemStack.EMPTY);
            }

            if (this.customName != null) {
                compound.putString("CustomName", Component.Serializer.toJson(this.customName));
            }

            compound.put("Container", this.mealContainerStack.serializeNBT());
            compound.put("Inventory", drops.serializeNBT());
            return compound;
        }
    }

    public static ItemStack getMealFromItem(ItemStack cookingPotStack) {
        if (!cookingPotStack.is((Item)NoGuiFD.NGCookingPotItem.get())) {
            return ItemStack.EMPTY;
        } else {
            CompoundTag compound = cookingPotStack.getTagElement("BlockEntityTag");
            if (compound != null) {
                CompoundTag inventoryTag = compound.getCompound("Inventory");
                if (inventoryTag.contains("Items", 9)) {
                    ItemStackHandler handler = new ItemStackHandler();
                    handler.deserializeNBT(inventoryTag);
                    return handler.getStackInSlot(6);
                }
            }

            return ItemStack.EMPTY;
        }
    }

    private Optional<CookingPotRecipe> getMatchingRecipe(RecipeWrapper inventoryWrapper) {
        if (this.level == null) {
            return Optional.empty();
        } else {
            if (this.lastRecipeID != null) {
                Recipe recipe = (Recipe)((RecipeManagerAccessor)this.level.getRecipeManager()).getRecipeMap((RecipeType) ModRecipeTypes.COOKING.get()).get(this.lastRecipeID);
                if (recipe instanceof CookingPotRecipe) {
                    if (recipe.matches(inventoryWrapper, this.level)) {
                        return Optional.of((CookingPotRecipe)recipe);
                    }

                    if (ItemStack.isSameItem(recipe.getResultItem(this.level.registryAccess()), this.getMeal())) {
                        return Optional.empty();
                    }
                }
            }

            if (this.checkNewRecipe) {
                Optional<CookingPotRecipe> recipe = this.level.getRecipeManager().getRecipeFor((RecipeType)ModRecipeTypes.COOKING.get(), inventoryWrapper, this.level);
                if (recipe.isPresent()) {
                    ResourceLocation newRecipeID = ((CookingPotRecipe)recipe.get()).getId();
                    if (this.lastRecipeID != null && !this.lastRecipeID.equals(newRecipeID)) {
                        this.cookTime = 0;
                    }

                    this.lastRecipeID = newRecipeID;
                    return recipe;
                }
            }

            this.checkNewRecipe = false;
            return Optional.empty();
        }
    }

    public ItemStack getContainer() {
        ItemStack mealStack = this.getMeal();
        return !mealStack.isEmpty() && !this.mealContainerStack.isEmpty() ? this.mealContainerStack : mealStack.getCraftingRemainingItem();
    }

    private boolean hasInput() {
        for(int i = 0; i < 6; ++i) {
            if (!this.inventory.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    protected boolean canCook(CookingPotRecipe recipe) {
        if (this.hasInput()) {
            ItemStack resultStack = recipe.getResultItem(this.level.registryAccess());
            if (resultStack.isEmpty()) {
                return false;
            } else {
                ItemStack storedMealStack = this.inventory.getStackInSlot(6);
                if (storedMealStack.isEmpty()) {
                    return true;
                } else if (!ItemStack.isSameItem(storedMealStack, resultStack)) {
                    return false;
                } else if (storedMealStack.getCount() + resultStack.getCount() <= this.inventory.getSlotLimit(6)) {
                    return true;
                } else {
                    return storedMealStack.getCount() + resultStack.getCount() <= resultStack.getMaxStackSize();
                }
            }
        } else {
            return false;
        }
    }

    private boolean processCooking(CookingPotRecipe recipe, NGCookingPotBlockEntity cookingPot) {
        if (this.level == null) {
            return false;
        } else {
            ++this.cookTime;
            this.cookTimeTotal = recipe.getCookTime();
            if (this.cookTime < this.cookTimeTotal) {
                return false;
            } else {
                this.cookTime = 0;
                this.mealContainerStack = recipe.getOutputContainer();
                ItemStack resultStack = recipe.getResultItem(this.level.registryAccess());
                ItemStack storedMealStack = this.inventory.getStackInSlot(6);
                if (storedMealStack.isEmpty()) {
                    this.inventory.setStackInSlot(6, resultStack.copy());
                } else if (ItemStack.isSameItem(storedMealStack, resultStack)) {
                    storedMealStack.grow(resultStack.getCount());
                }

                cookingPot.setRecipeUsed(recipe);

                for(int i = 0; i < 6; ++i) {
                    ItemStack slotStack = this.inventory.getStackInSlot(i);
                    if (slotStack.hasCraftingRemainingItem()) {
                        this.ejectIngredientRemainder(slotStack.getCraftingRemainingItem());
                    } else if (INGREDIENT_REMAINDER_OVERRIDES.containsKey(slotStack.getItem())) {
                        this.ejectIngredientRemainder(((Item)INGREDIENT_REMAINDER_OVERRIDES.get(slotStack.getItem())).getDefaultInstance());
                    }

                    if (!slotStack.isEmpty()) {
                        slotStack.shrink(1);
                    }
                }

                return true;
            }
        }
    }

    protected void ejectIngredientRemainder(ItemStack remainderStack) {
        Direction direction = ((Direction)this.getBlockState().getValue(CookingPotBlock.FACING)).getCounterClockWise();
        double x = (double)this.worldPosition.getX() + 0.5D + (double)direction.getStepX() * 0.25D;
        double y = (double)this.worldPosition.getY() + 0.7D;
        double z = (double)this.worldPosition.getZ() + 0.5D + (double)direction.getStepZ() * 0.25D;
        ItemUtils.spawnItemEntity(this.level, remainderStack, x, y, z, (double)((float)direction.getStepX() * 0.08F), 0.25D, (double)((float)direction.getStepZ() * 0.08F));
    }

    public void setRecipeUsed(@javax.annotation.Nullable Recipe<?> recipe) {
        if (recipe != null) {
            ResourceLocation recipeID = recipe.getId();
            this.usedRecipeTracker.addTo(recipeID, 1);
        }

    }

    @javax.annotation.Nullable
    public Recipe<?> getRecipeUsed() {
        return null;
    }

    public void awardUsedRecipes(Player player, List<ItemStack> items) {
        List<Recipe<?>> usedRecipes = this.getUsedRecipesAndPopExperience(player.level(), player.position());
        player.awardRecipes(usedRecipes);
        this.usedRecipeTracker.clear();
    }

    public List<Recipe<?>> getUsedRecipesAndPopExperience(Level level, Vec3 pos) {
        List<Recipe<?>> list = Lists.newArrayList();
        ObjectIterator var4 = this.usedRecipeTracker.object2IntEntrySet().iterator();

        while(var4.hasNext()) {
            Object2IntMap.Entry<ResourceLocation> entry = (Object2IntMap.Entry)var4.next();
            level.getRecipeManager().byKey((ResourceLocation)entry.getKey()).ifPresent((recipe) -> {
                list.add(recipe);
                splitAndSpawnExperience((ServerLevel)level, pos, entry.getIntValue(), ((CookingPotRecipe)recipe).getExperience());
            });
        }

        return list;
    }

    private static void splitAndSpawnExperience(ServerLevel level, Vec3 pos, int craftedAmount, float experience) {
        int expTotal = Mth.floor((float)craftedAmount * experience);
        float expFraction = Mth.frac((float)craftedAmount * experience);
        if (expFraction != 0.0F && Math.random() < (double)expFraction) {
            ++expTotal;
        }

        ExperienceOrb.award(level, pos, expTotal);
    }

    public boolean isHeated() {
        return this.level != null && this.isHeated(this.level, this.worldPosition);
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    public ItemStack getMeal() {
        return this.inventory.getStackInSlot(6);
    }

    public NonNullList<ItemStack> getDroppableInventory() {
        NonNullList<ItemStack> drops = NonNullList.create();

        for(int i = 0; i < 9; ++i) {
            if (i != 6) {
                drops.add(this.inventory.getStackInSlot(i));
            }
        }

        return drops;
    }

    private void moveMealToOutput() {
        ItemStack mealStack = this.inventory.getStackInSlot(6);
        ItemStack outputStack = this.inventory.getStackInSlot(8);
        int mealCount = Math.min(mealStack.getCount(), mealStack.getMaxStackSize() - outputStack.getCount());
        if (outputStack.isEmpty()) {
            this.inventory.setStackInSlot(8, mealStack.split(mealCount));
        } else if (outputStack.getItem() == mealStack.getItem()) {
            mealStack.shrink(mealCount);
            outputStack.grow(mealCount);
        }

    }

    private void useStoredContainersOnMeal() {
        ItemStack mealStack = this.inventory.getStackInSlot(6);
        ItemStack containerInputStack = this.inventory.getStackInSlot(7);
        ItemStack outputStack = this.inventory.getStackInSlot(8);
        if (this.isContainerValid(containerInputStack) && outputStack.getCount() < outputStack.getMaxStackSize()) {
            int smallerStackCount = Math.min(mealStack.getCount(), containerInputStack.getCount());
            int mealCount = Math.min(smallerStackCount, mealStack.getMaxStackSize() - outputStack.getCount());
            if (outputStack.isEmpty()) {
                containerInputStack.shrink(mealCount);
                this.inventory.setStackInSlot(8, mealStack.split(mealCount));
            } else if (outputStack.getItem() == mealStack.getItem()) {
                mealStack.shrink(mealCount);
                containerInputStack.shrink(mealCount);
                outputStack.grow(mealCount);
            }
        }

    }

    public ItemStack useHeldItemOnMeal(ItemStack container) {
        if (this.isContainerValid(container) && !this.getMeal().isEmpty()) {
            //container.shrink(1);
            ItemStack itemStack = this.inventory.getStackInSlot(6);
            itemStack.setCount(itemStack.getCount());
            this.inventory.setStackInSlot(6,itemStack);
            return this.getMeal().split(1);
        } else {
            return ItemStack.EMPTY;
        }
    }

    private boolean doesMealHaveContainer(ItemStack meal) {
        return !this.mealContainerStack.isEmpty() || meal.hasCraftingRemainingItem();
    }

    public boolean isContainerValid(ItemStack containerItem) {
        if (containerItem.isEmpty()) {
            return false;
        } else {
            return !this.mealContainerStack.isEmpty() ? ItemStack.isSameItem(this.mealContainerStack, containerItem) : ItemStack.isSameItem(this.getMeal(), containerItem);
        }
    }

    public Component getName() {
        return (Component)(this.customName != null ? this.customName : TextUtils.getTranslation("container.cooking_pot", new Object[0]));
    }

    public Component getDisplayName() {
        return this.getName();
    }

    @javax.annotation.Nullable
    public Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    public AbstractContainerMenu createMenu(int id, Inventory player, Player entity) {
        return new NGCookingPotMenu(id, player, this, this.cookingPotData);
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(ForgeCapabilities.ITEM_HANDLER)) {
            return side != null && !side.equals(Direction.UP) ? this.outputHandler.cast() : this.inputHandler.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    public void setRemoved() {
        super.setRemoved();
        this.inputHandler.invalidate();
        this.outputHandler.invalidate();
    }

    public CompoundTag getUpdateTag() {
        return this.writeItems(new CompoundTag());
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(9) {
            protected void onContentsChanged(int slot) {
                if (slot >= 0 && slot < 6) {
                    NGCookingPotBlockEntity.this.checkNewRecipe = true;
                }

                NGCookingPotBlockEntity.this.inventoryChanged();
            }
        };
    }

    private ContainerData createIntArray() {
        return new ContainerData() {
            public int get(int index) {
                int var10000;
                switch(index) {
                    case 0:
                        var10000 = NGCookingPotBlockEntity.this.cookTime;
                        break;
                    case 1:
                        var10000 = NGCookingPotBlockEntity.this.cookTimeTotal;
                        break;
                    default:
                        var10000 = 0;
                }

                return var10000;
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        NGCookingPotBlockEntity.this.cookTime = value;
                        break;
                    case 1:
                        NGCookingPotBlockEntity.this.cookTimeTotal = value;
                }

            }

            public int getCount() {
                return 2;
            }
        };
    }

    static {
        INGREDIENT_REMAINDER_OVERRIDES = Map.ofEntries(Map.entry(Items.POWDER_SNOW_BUCKET, Items.BUCKET), Map.entry(Items.AXOLOTL_BUCKET, Items.BUCKET), Map.entry(Items.COD_BUCKET, Items.BUCKET), Map.entry(Items.PUFFERFISH_BUCKET, Items.BUCKET), Map.entry(Items.SALMON_BUCKET, Items.BUCKET), Map.entry(Items.TROPICAL_FISH_BUCKET, Items.BUCKET), Map.entry(Items.SUSPICIOUS_STEW, Items.BOWL), Map.entry(Items.MUSHROOM_STEW, Items.BOWL), Map.entry(Items.RABBIT_STEW, Items.BOWL), Map.entry(Items.BEETROOT_SOUP, Items.BOWL), Map.entry(Items.POTION, Items.GLASS_BOTTLE), Map.entry(Items.SPLASH_POTION, Items.GLASS_BOTTLE), Map.entry(Items.LINGERING_POTION, Items.GLASS_BOTTLE), Map.entry(Items.EXPERIENCE_BOTTLE, Items.GLASS_BOTTLE));
    }



    public static void animationTick(Level level, BlockPos pos, BlockState blockState, BlockEntity cookingPot2) {
        NGCookingPotBlockEntity cookingPot = (NGCookingPotBlockEntity) cookingPot2;
        if (cookingPot.isHeated(level, pos)) {
            RandomSource random = level.random;
            double x;
            double y;
            double z;
            if (random.nextFloat() < 0.2F) {
                x = (double)pos.getX() + 0.5D + (random.nextDouble() * 0.6D - 0.3D);
                y = (double)pos.getY() + 0.7D;
                z = (double)pos.getZ() + 0.5D + (random.nextDouble() * 0.6D - 0.3D);
                level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0.0D, 0.0D, 0.0D);
            }

            if (random.nextFloat() < 0.03F) {
                x = (double)pos.getX() + 0.5D + (random.nextDouble() * 0.4D - 0.2D);
                y = (double)pos.getY() + 0.5D;
                z = (double)pos.getZ() + 0.5D + (random.nextDouble() * 0.4D - 0.2D);
                double motionY = random.nextBoolean() ? 0.015D : 0.005D;
                level.addParticle((ParticleOptions)ModParticleTypes.STEAM.get(), x, y, z, 0.0D, motionY, 0.0D);
            }
        }
    }

    public static void cookingTick(Level level, BlockPos pos, BlockState blockState, BlockEntity cookingPot2) {
        NGCookingPotBlockEntity cookingPot = (NGCookingPotBlockEntity) cookingPot2;
        boolean isHeated = cookingPot.isHeated(level, pos);
        boolean didInventoryChange = false;
        if (isHeated && cookingPot.hasInput()) {
            Optional<CookingPotRecipe> recipe = cookingPot.getMatchingRecipe(new RecipeWrapper(cookingPot.inventory));
            if (recipe.isPresent() && cookingPot.canCook((CookingPotRecipe)recipe.get())) {
                didInventoryChange = cookingPot.processCooking((CookingPotRecipe)recipe.get(), cookingPot);
            } else {
                cookingPot.cookTime = 0;
            }
        } else if (cookingPot.cookTime > 0) {
            cookingPot.cookTime = Mth.clamp(cookingPot.cookTime - 2, 0, cookingPot.cookTimeTotal);
        }

        ItemStack mealStack = cookingPot.getMeal();
        if (!mealStack.isEmpty()) {
            if (!cookingPot.doesMealHaveContainer(mealStack)) {
                cookingPot.moveMealToOutput();
                didInventoryChange = true;
            } else if (!cookingPot.inventory.getStackInSlot(7).isEmpty()) {
                cookingPot.useStoredContainersOnMeal();
                didInventoryChange = true;
            }
        }

        if (didInventoryChange) {
            cookingPot.inventoryChanged();
        }
    }

}