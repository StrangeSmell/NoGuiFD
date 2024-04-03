package com.strangesmell.noguifd;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;


import static com.strangesmell.noguifd.NGCookingPotBlock.FACING;

public class NGCookingPotBlockEntityRenderer implements BlockEntityRenderer<NGCookingPotBlockEntity> {
    private final ItemRenderer itemRenderer;
    private static double onePix =0.0625;
    public NGCookingPotBlockEntityRenderer(BlockEntityRendererProvider.Context p_173602_) {
        this.itemRenderer = p_173602_.getItemRenderer();
    }

    @Override
    public void render(NGCookingPotBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        for (int index = 0 ; index<pBlockEntity.getInventory().getSlots();index++){
            ItemStack itemStack = pBlockEntity.getInventory().getStackInSlot(index);
            if(itemStack.isEmpty()) {
                pBlockEntity.decreaseIndexCount(index);
                continue;
            }
            if(index<6) renderItem(pBlockEntity,pPoseStack,index,itemStack,pPackedLight,pPackedOverlay,pBuffer,pPartialTick);
            else renderItem2(pBlockEntity,pPoseStack,index,itemStack,pPackedLight,pPackedOverlay,pBuffer,pPartialTick);
        }
    }

    public void renderItem2(NGCookingPotBlockEntity pBlockEntity, PoseStack pPoseStack, int index,ItemStack itemStack, int pPackedLight, int pPackedOverlay, MultiBufferSource pBuffer, float pPartialTick) {
        Direction face = pBlockEntity.getBlockState().getValue(FACING);
        double dy = 11.25;
        HitResult hitResult = Minecraft.getInstance().player.pick(5, pPartialTick,false);
        Vec3 viewPose =hitResult.getLocation();
        BlockPos blockPos = pBlockEntity.getBlockPos();
        double dx =viewPose.get(Direction.Axis.X)-blockPos.getX();
        double dz =viewPose.get(Direction.Axis.Z)-blockPos.getZ();
        pPoseStack.pushPose();
        float time = 0;
        if(pBlockEntity.getLevel()!=null)  time = ((float)pBlockEntity.getLevel().getGameTime());
        switch (face){
            case EAST -> {
                switch (index){
                    case 6-> {
                        pPoseStack.translate(8*onePix,onePix*dy,3*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(10*onePix)&&dx>(6*onePix)&&dz>(2*onePix)&&dz<(4*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        }else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                    case 7-> {
                        pPoseStack.translate(12.5*onePix,onePix*dy,13*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(14*onePix)&&dx>(11*onePix)&&dz>(12*onePix)&&dz<(14*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        } else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                    case 8-> {
                        pPoseStack.translate(3.5*onePix,onePix*dy,13*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(5*onePix)&&dx>(2*onePix)&&dz>(12*onePix)&&dz<(14*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        }else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                }
                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
                pPoseStack.popPose();
            }
            case WEST -> {
                switch (index){
                    case 6-> {
                        pPoseStack.translate(8*onePix,onePix*dy,13*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(10*onePix)&&dx>(6*onePix)&&dz>(12*onePix)&&dz<(14*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        }else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                    case 7-> {
                        pPoseStack.translate(12.5*onePix,onePix*dy,3*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(14*onePix)&&dx>(11*onePix)&&dz>(2*onePix)&&dz<(4*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        } else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                    case 8-> {
                        pPoseStack.translate(3.5*onePix,onePix*dy,3*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(5*onePix)&&dx>(2*onePix)&&dz>(2*onePix)&&dz<(4*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        }else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                }
                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
                pPoseStack.popPose();
            }
            case SOUTH -> {
                switch (index){
                    case 6-> {
                        pPoseStack.translate(13*onePix,onePix*dy,8*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(14*onePix)&&dx>(12*onePix)&&dz>(6*onePix)&&dz<(10*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        }else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                    case 7-> {
                        pPoseStack.translate(3*onePix,onePix*dy,3.5*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(4*onePix)&&dx>(2*onePix)&&dz>(2*onePix)&&dz<(5*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        } else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                    case 8-> {
                        pPoseStack.translate(3*onePix,onePix*dy,12.5*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(4*onePix)&&dx>(2*onePix)&&dz>(11*onePix)&&dz<(14*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        }else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                }
                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
                pPoseStack.popPose();
            }
            case NORTH -> {
                switch (index){
                    case 6-> {
                        pPoseStack.translate(3*onePix,onePix*dy,8*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(4*onePix)&&dx>(2*onePix)&&dz>(6*onePix)&&dz<(10*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        }else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                    case 7-> {
                        pPoseStack.translate(13*onePix,onePix*dy,3.5*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(14*onePix)&&dx>(12*onePix)&&dz>(2*onePix)&&dz<(5*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        } else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                    case 8-> {
                        pPoseStack.translate(13*onePix,onePix*dy,12.5*onePix);
                        pPoseStack.scale(0.2F, 0.2F, 0.2F);
                        pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees(time));
                        if(dx<(14*onePix)&&dx>(12*onePix)&&dz>(11*onePix)&&dz<(14*onePix)){
                            pBlockEntity.increaseIndexCount(index);
                        }else {
                            pBlockEntity.decreaseIndexCount(index);
                        }
                    }
                }
                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
                pPoseStack.popPose();
            }
        }
    }

    public void renderItem(NGCookingPotBlockEntity pBlockEntity, PoseStack pPoseStack, int index,ItemStack itemStack, int pPackedLight, int pPackedOverlay, MultiBufferSource pBuffer, float pPartialTick){
        Direction face = pBlockEntity.getBlockState().getValue(FACING);
        int line = index / 3 ;
        int row = index  % 3 ;
        double dy = 10.25;
        double dd=1.3333f;
        HitResult hitResult = Minecraft.getInstance().player.pick(5, pPartialTick,false);
        Vec3 viewPose =hitResult.getLocation();
        BlockPos blockPos = pBlockEntity.getBlockPos();
        double dx =viewPose.get(Direction.Axis.X)-blockPos.getX();
        double dz =viewPose.get(Direction.Axis.Z)-blockPos.getZ();
        Vec2 animationVec = renderAnimation(index,pBlockEntity, face);
        float x= animationVec.x/30;
        float y= animationVec.y/30;

        float rollX = pBlockEntity.randomRollX.nextFloat(1f)*0.8f-0.1f+pBlockEntity.randomVecX.nextFloat();
        float rollY = pBlockEntity.randomRollY.nextFloat(1f)*0.5f-0.1f+pBlockEntity.randomVecY.nextFloat();
        float rollZ = pBlockEntity.randomRollZ.nextFloat(1f)*0.5f-0.1f+pBlockEntity.randomVecZ.nextFloat();
        if(index<6&&pBlockEntity.isHeated()){
              pBlockEntity.rollCountX.put(index,pBlockEntity.rollCountX.get(index)+rollX);
              pBlockEntity.rollCountY.put(index,pBlockEntity.rollCountY.get(index)+rollY);
              pBlockEntity.rollCountZ.put(index,pBlockEntity.rollCountZ.get(index)+rollZ);
        }

        switch (face){
            case EAST -> {

                pPoseStack.pushPose();
                pPoseStack.translate(5.5*onePix,onePix*dy,(4+dd)*onePix);
                pPoseStack.translate(row*dd*2*onePix,0,line*3*onePix);
                pPoseStack.scale(0.2F, 0.2F, 0.2F);
                //pPoseStack.translate(0,Math.sin(time/10)/5,0);
                pPoseStack.translate(x*x*x/5.7,0,y*y*y/5.7);
                pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(pBlockEntity.rollCountX.get(index)));
                pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.rollCountY.get(index)));
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(pBlockEntity.rollCountZ.get(index)));
                if(dx<(7*onePix+row*3*onePix)&&dx>(4*onePix+row*3*onePix)&&dz>(4*onePix+line*dd*2*onePix)&&dz<((4+2*dd)*onePix+line*dd*2*onePix)){
                    //pPoseStack.scale(1.25F, 1.25F, 1.25F);
                    pBlockEntity.increaseIndexCount(index);
                }else {
                    pBlockEntity.decreaseIndexCount(index);
                }
                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
                pPoseStack.popPose();
            }
            case WEST -> {
                pPoseStack.pushPose();
                pPoseStack.translate((4+dd*5)*onePix,onePix*dy,10.5*onePix);
                pPoseStack.translate(-row*dd*2*onePix,0,-line*3*onePix);
                pPoseStack.scale(0.2F, 0.2F, 0.2F);
                pPoseStack.translate(-x*x*x/5.7,0,-y*y*y/5.7);
                pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(pBlockEntity.rollCountX.get(index)));
                pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.rollCountY.get(index)));
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(pBlockEntity.rollCountZ.get(index)));
                if(dx<(12*onePix-row*dd*2*onePix)&&dx>(12*onePix-(row+1)*dd*2*onePix)&&dz>(9*onePix-line*3*onePix)&&dz<(12*onePix-line*3*onePix)){
                    //pPoseStack.scale(1.25F, 1.25F, 1.25F);
                    pBlockEntity.increaseIndexCount(index);
                }else {
                    pBlockEntity.decreaseIndexCount(index);
                }
                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
                pPoseStack.popPose();
            }
            case SOUTH -> {
                pPoseStack.pushPose();
                pPoseStack.translate(10.5*onePix,onePix*dy,(4+dd)*onePix);
                pPoseStack.translate(-line*3*onePix,0,row*dd*2*onePix);
                pPoseStack.scale(0.2F, 0.2F, 0.2F);
                pPoseStack.translate(x*x*x/5.7,0,y*y*y/5.7);
                pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(pBlockEntity.rollCountX.get(index)));
                pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.rollCountY.get(index)));
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(pBlockEntity.rollCountZ.get(index)));
                if(dx<(12*onePix-line*3*onePix)&&dx>(9*onePix-line*3*onePix)&&dz>(4*onePix+row*dd*2*onePix)&&dz<((4+2*dd)*onePix+row*dd*2*onePix)){
                    //pPoseStack.scale(1.25F, 1.25F, 1.25F);
                    pBlockEntity.increaseIndexCount(index);
                }else {
                    pBlockEntity.decreaseIndexCount(index);
                }
                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
                pPoseStack.popPose();
            }
            case NORTH -> {
                pPoseStack.pushPose();
                pPoseStack.translate(5.5*onePix,onePix*dy,(4+dd*5)*onePix);
                pPoseStack.translate(line*3*onePix,0,-row*dd*2*onePix);
                pPoseStack.scale(0.2F, 0.2F, 0.2F);
                pPoseStack.translate(-x*x*x/5.7,0,-y*y*y/5.7);
                pPoseStack.translate(0,pBlockEntity.indexCount.get(index)/80,0);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(pBlockEntity.rollCountX.get(index)));
                pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.rollCountY.get(index)));
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(pBlockEntity.rollCountZ.get(index)));
                if(dx<(7*onePix+line*3*onePix)&&dx>(4*onePix+line*3*onePix)&&dz>((12-2*dd)*onePix-row*dd*2*onePix)&&dz<(12*onePix-row*dd*2*onePix)){
                    //pPoseStack.scale(1.25F, 1.25F, 1.25F);
                    pBlockEntity.increaseIndexCount(index);
                }else {
                    pBlockEntity.decreaseIndexCount(index);
                }
                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
                pPoseStack.popPose();
            }
        }
    }



    public Vec2 renderAnimation(int index, NGCookingPotBlockEntity blockEntity,Direction face ){
        if(face==Direction.EAST||face==Direction.WEST){
            float toRight=0 ,toLeft=0,toDown=0,toUp =0;
            if(index%3!=0){//左边有物品
                toRight = blockEntity.indexCount.get(index-1);
            }
            if(index%3!=2){//右边有物品
                toLeft = - blockEntity.indexCount.get(index+1);
            }
            if((index-3)>=0){//上面有物品
                toDown =blockEntity.indexCount.get(index-3);
            }
            if((index+3)<=5){//下面有物品
                toUp = - blockEntity.indexCount.get(index+3);
            }
            return new Vec2(toLeft+toRight,toDown+toUp);
        }else{
            float toRight=0 ,toLeft=0,toDown=0,toUp =0;
            if(index+3<6){//左边有物品
                toRight = blockEntity.indexCount.get(index+3);
            }
            if(index-3>=0){//右边有物品
                toLeft = - blockEntity.indexCount.get(index-3);
            }
            if(index%3!=0){//上面有物品
                toDown =blockEntity.indexCount.get(index-1);
            }
            if(index%3!=2){//下面有物品
                toUp = - blockEntity.indexCount.get(index+1);
            }
            return new Vec2(toLeft+toRight,toDown+toUp);
        }

    }


}
