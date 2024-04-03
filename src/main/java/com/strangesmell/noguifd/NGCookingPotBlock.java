package com.strangesmell.noguifd;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import vectorwing.farmersdelight.common.block.CookingPotBlock;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.state.CookingPotSupport;
import vectorwing.farmersdelight.common.tag.ModTags;

import javax.annotation.Nullable;

public class NGCookingPotBlock extends CookingPotBlock {
    public static final DirectionProperty FACING;
    public static final EnumProperty<CookingPotSupport> SUPPORT;
    public static final BooleanProperty WATERLOGGED;
    protected static final VoxelShape SHAPE;
    protected static final VoxelShape SHAPE_WITH_TRAY;

    public NGCookingPotBlock() {
        super(Properties.of().mapColor(MapColor.METAL).strength(0.5F, 6.0F).sound(SoundType.LANTERN));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SUPPORT, CookingPotSupport.NONE).setValue(WATERLOGGED, false));

    }
    private static double onePix =0.0625;
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult result) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty() && player.isShiftKeyDown()) {
            level.setBlockAndUpdate(pos, state.setValue(SUPPORT, state.getValue(SUPPORT).equals(CookingPotSupport.HANDLE)
                    ? getTrayState(level, pos) : CookingPotSupport.HANDLE));
            level.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.7F, 1.0F);
        } else if (!level.isClientSide) {
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if (tileEntity instanceof NGCookingPotBlockEntity cookingPotEntity) {

                Direction hitFace = result.getDirection();
                Vec3 viewPose = result.getLocation();
                if(hitFace!=Direction.UP){//不是上面
                    ItemStack servingStack = cookingPotEntity.useHeldItemOnMeal(heldStack);
                    if (servingStack != ItemStack.EMPTY) {
                        if (!player.getInventory().add(servingStack)) {
                            player.drop(servingStack, false);
                        }
                        level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
                    } else {
                        NetworkHooks.openScreen((ServerPlayer) player, cookingPotEntity, pos);
                    }
                }else{//是上面
                    double dx =viewPose.get(Direction.Axis.X)-pos.getX();
                    double dz =viewPose.get(Direction.Axis.Z)-pos.getZ();
                    int index = (int) ((dx-2*onePix)/(onePix*4))+((int)((dz-2*onePix)/(onePix*4))*3);

                    difFaceSetItem(pos,cookingPotEntity,cookingPotEntity.getBlockState().getValue(FACING),viewPose,player,hand,level,pos);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    public void difFaceSetItem(BlockPos blockPos,NGCookingPotBlockEntity ngCookingPotBlockEntity,Direction face, Vec3 viewPose ,Player pPlayer, InteractionHand pHand,Level level,BlockPos pos){
        double dd=1.3333f;
        switch (face){
                case EAST ->{
                    double dx = viewPose.get(Direction.Axis.X)-blockPos.getX() ;
                    double dz = viewPose.get(Direction.Axis.Z)-blockPos.getZ() ;
                    switch ((int) (dz/onePix)){
                        case 2 , 3 ->{
                            if(dx>6*onePix&&dx<10*onePix) {
                                ItemStack heldStack = pPlayer.getItemInHand(pHand);
                                ItemStack servingStack = ngCookingPotBlockEntity.useHeldItemOnMeal(heldStack);
                                if (servingStack != ItemStack.EMPTY) {
                                    if (!pPlayer.getInventory().add(servingStack)) {
                                        pPlayer.drop(servingStack, false);
                                    }
                                    level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
                                }
                            }
                        }
                        case 4,5,6 ->{
                            int indexTemp = (int) ((dx-4*onePix)/(2*dd*onePix));
                            setItem(indexTemp,pPlayer,pHand,ngCookingPotBlockEntity);
                        }
                        case 7,8,9 ->{
                            int indexTemp = (int) ((dx-4*onePix)/(2*dd*onePix));
                            setItem(indexTemp+3,pPlayer,pHand,ngCookingPotBlockEntity);
                        }
                        case 12,13->{
                            switch ((int) (dx/onePix)){
                                case 11,12,13 -> {
                                    if ( !ngCookingPotBlockEntity.getInventory().getStackInSlot(8).isEmpty()) {
                                        ItemStack itemStack = ngCookingPotBlockEntity.getInventory().getStackInSlot(8);
                                        if (!pPlayer.getInventory().add(itemStack)) {
                                            pPlayer.drop(itemStack, false);
                                        }
                                        ngCookingPotBlockEntity.getInventory().setStackInSlot(8,new ItemStack(Items.AIR));
                                    }
                                }
                                case 2,3,4-> setItem(7,pPlayer,pHand,ngCookingPotBlockEntity);
                            }
                        }

                    }
                }
                case WEST -> {
                    double dx = blockPos.getX() - viewPose.get(Direction.Axis.X) + 1 ;
                    double dz = blockPos.getZ() - viewPose.get(Direction.Axis.Z) + 1 ;
                    switch ((int) (dz/onePix)){
                        case 2 , 3 ->{
                            if(dx>6*onePix&&dx<10*onePix) {
                                ItemStack heldStack = pPlayer.getItemInHand(pHand);
                                ItemStack servingStack = ngCookingPotBlockEntity.useHeldItemOnMeal(heldStack);
                                if (servingStack != ItemStack.EMPTY) {
                                    if (!pPlayer.getInventory().add(servingStack)) {
                                        pPlayer.drop(servingStack, false);
                                    }
                                    level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
                                }
                            }
                        }
                        case 4,5,6 ->{
                            int indexTemp = (int) ((dx-4*onePix)/(2*dd*onePix));
                            setItem(indexTemp,pPlayer,pHand,ngCookingPotBlockEntity);
                        }
                        case 7,8,9 ->{
                            int indexTemp = (int) ((dx-4*onePix)/(2*dd*onePix));
                            setItem(indexTemp+3,pPlayer,pHand,ngCookingPotBlockEntity);
                        }
                        case 12,13->{
                            switch ((int) (dx/onePix)){
                                case 11,12,13-> {
                                    if ( !ngCookingPotBlockEntity.getInventory().getStackInSlot(8).isEmpty()) {
                                        ItemStack itemStack = ngCookingPotBlockEntity.getInventory().getStackInSlot(8);
                                        if (!pPlayer.getInventory().add(itemStack)) {
                                            pPlayer.drop(itemStack, false);
                                        }
                                        ngCookingPotBlockEntity.getInventory().setStackInSlot(8,new ItemStack(Items.AIR));
                                    }
                                }
                                case 2,3,4-> setItem(7,pPlayer,pHand,ngCookingPotBlockEntity);
                            }
                        }

                    }
                }
                case SOUTH -> {
                    double dx = viewPose.get(Direction.Axis.X) - blockPos.getX() ;
                    double dz = viewPose.get(Direction.Axis.Z)-blockPos.getZ() ;
                    switch ((int) (dx/onePix)){
                        case 2 , 3 ->{
                            switch ((int) (dz/onePix)){
                                case 2,3,4 -> setItem(7,pPlayer,pHand,ngCookingPotBlockEntity);
                                case 11,12,13->{
                                    if ( !ngCookingPotBlockEntity.getInventory().getStackInSlot(8).isEmpty()) {
                                        ItemStack itemStack = ngCookingPotBlockEntity.getInventory().getStackInSlot(8);
                                        if (!pPlayer.getInventory().add(itemStack)) {
                                            pPlayer.drop(itemStack, false);
                                        }
                                        ngCookingPotBlockEntity.getInventory().setStackInSlot(8,new ItemStack(Items.AIR));
                                    }
                                }
                            }

                        }
                        case 6,7,8 ->{
                            int indexTemp = (int) ((dz-4*onePix)/(2*dd*onePix));
                            setItem(indexTemp+3,pPlayer,pHand,ngCookingPotBlockEntity);
                        }
                        case 9,10,11 ->{
                            int indexTemp = (int) ((dz-4*onePix)/(2*dd*onePix));
                            setItem(indexTemp,pPlayer,pHand,ngCookingPotBlockEntity);
                        }
                        case 12,13->{
                            if(dz>6*onePix&&dz<10*onePix) {
                                ItemStack heldStack = pPlayer.getItemInHand(pHand);
                                ItemStack servingStack = ngCookingPotBlockEntity.useHeldItemOnMeal(heldStack);
                                if (servingStack != ItemStack.EMPTY) {
                                    if (!pPlayer.getInventory().add(servingStack)) {
                                        pPlayer.drop(servingStack, false);
                                    }
                                    level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
                                }
                            }
                        }

                    }
                }
                case NORTH -> {
                    double dx =blockPos.getX()+1- viewPose.get(Direction.Axis.X) ;
                    double dz =blockPos.getZ()+1- viewPose.get(Direction.Axis.Z) ;
                    switch ((int) (dx/onePix)){
                        case 2 , 3 ->{
                            switch ((int) (dz/onePix)){
                                case 11,12,13-> setItem(7,pPlayer,pHand,ngCookingPotBlockEntity);
                                case 2,3,4->{
                                    if ( !ngCookingPotBlockEntity.getInventory().getStackInSlot(8).isEmpty()) {
                                        ItemStack itemStack = ngCookingPotBlockEntity.getInventory().getStackInSlot(8);
                                        if (!pPlayer.getInventory().add(itemStack)) {
                                            pPlayer.drop(itemStack, false);
                                        }
                                        ngCookingPotBlockEntity.getInventory().setStackInSlot(8,new ItemStack(Items.AIR));
                                    }
                                }
                            }

                        }
                        case 6,7,8 ->{
                            int indexTemp = (int) ((dz-4*onePix)/(2*dd*onePix));
                            setItem(indexTemp+3,pPlayer,pHand,ngCookingPotBlockEntity);
                        }
                        case 9,10,11 ->{
                            int indexTemp = (int) ((dz-4*onePix)/(2*dd*onePix));
                            setItem(indexTemp,pPlayer,pHand,ngCookingPotBlockEntity);
                        }
                        case 12,13->{
                            if(dz>6*onePix&&dz<10*onePix) {
                                ItemStack heldStack = pPlayer.getItemInHand(pHand);
                                ItemStack servingStack = ngCookingPotBlockEntity.useHeldItemOnMeal(heldStack);
                                if (servingStack != ItemStack.EMPTY) {
                                    if (!pPlayer.getInventory().add(servingStack)) {
                                        pPlayer.drop(servingStack, false);
                                    }
                                    level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
                                }
                            }
                        }

                    }
                }
            }
    }

    public static void setItem(int index, Player player, InteractionHand pHand, NGCookingPotBlockEntity ngCookingPotBlockEntity ){

        ItemStack useItemStack = player.getItemInHand(pHand);
        ItemStackHandler inventory = ngCookingPotBlockEntity.getInventory();
        ItemStack chestItemStack = inventory.getStackInSlot(index);
        if(useItemStack.isEmpty()&&chestItemStack.isEmpty()) return;

        if (!useItemStack.isEmpty()&&chestItemStack.isEmpty()) {
            inventory.setStackInSlot(index,useItemStack);
            player.setItemInHand(pHand,new ItemStack(Items.AIR));
        }
        if (useItemStack.isEmpty()&&!chestItemStack.isEmpty()) {
            player.setItemInHand(pHand,chestItemStack);
            inventory.setStackInSlot(index,new ItemStack(Items.AIR));
        }
        if (!useItemStack.isEmpty()&&!chestItemStack.isEmpty()) {
            inventory.setStackInSlot(index,useItemStack);
            player.setItemInHand(pHand, chestItemStack);
        }


    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if (tileEntity instanceof NGCookingPotBlockEntity) {
                NGCookingPotBlockEntity cookingPotEntity = (NGCookingPotBlockEntity)tileEntity;
                Containers.dropContents(level, pos, cookingPotEntity.getDroppableInventory());
                cookingPotEntity.getUsedRecipesAndPopExperience(level, Vec3.atCenterOf(pos));
                level.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }

    }

    private CookingPotSupport getTrayState(LevelAccessor level, BlockPos pos) {
        if (level.getBlockState(pos.below()).is(ModTags.TRAY_HEAT_SOURCES)) {
            return CookingPotSupport.TRAY;
        }
        return CookingPotSupport.NONE;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        //return super.getTicker(level,state,blockEntity) ;
        return level.isClientSide ? createTickerHelper(blockEntity, (BlockEntityType)NoGuiFD.NGCookingPotEntity.get(), NGCookingPotBlockEntity::animationTick) : createTickerHelper(blockEntity, (BlockEntityType)NoGuiFD.NGCookingPotEntity.get(), NGCookingPotBlockEntity::cookingTick);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        NGCookingPotBlockEntity cookingPotEntity = (NGCookingPotBlockEntity)level.getBlockEntity(pos);
        if (cookingPotEntity != null) {
            CompoundTag nbt = cookingPotEntity.writeMeal(new CompoundTag());
            if (!nbt.isEmpty()) {
                stack.addTagElement("BlockEntityTag", nbt);
            }

            if (cookingPotEntity.hasCustomName()) {
                stack.setHoverName(cookingPotEntity.getCustomName());
            }
        }

        return stack;
    }
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        //return ((BlockEntityType) NoGuiFD.NGCookingPotEntity.get()).create(pos, state);
        return new NGCookingPotBlockEntity(pos,state);
    }
    static {
        FACING = BlockStateProperties.HORIZONTAL_FACING;
        SUPPORT = EnumProperty.create("support", CookingPotSupport.class);
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 10.0D, 14.0D);
        SHAPE_WITH_TRAY = Shapes.or(SHAPE, Block.box(0.0D, -1.0D, 0.0D, 16.0D, 0.0D, 16.0D));
    }
}
