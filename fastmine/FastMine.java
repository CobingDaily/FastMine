package fastmine;

import fastmine.commandos.FastMineCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class FastMine {
    public static boolean toggled = false;
    private final double speed = 1.4;
    private boolean destroyingBlock;
    private float destroyProgress;
    private EnumFacing blockFacing;
    private BlockPos blockPos;
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new FastMineCommand());


    }

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        event.manager.channel().pipeline().addLast("LaxBit", new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (Minecraft.getMinecraft().playerController != null && !Minecraft.getMinecraft().playerController.isInCreativeMode() && msg instanceof C07PacketPlayerDigging && toggled) {
                    C07PacketPlayerDigging packetPlayerDigging = (C07PacketPlayerDigging) msg;
                    if (packetPlayerDigging.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        destroyingBlock = true;
                        blockPos = packetPlayerDigging.getPosition();
                        blockFacing = packetPlayerDigging.getFacing();
                        destroyProgress = 0.0f;
                    }
                    if (packetPlayerDigging.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK || packetPlayerDigging.getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                        destroyingBlock = false;
                        blockPos = null;
                        blockFacing = null;
                    }
                }
                super.write(ctx, msg, promise);
            }
        });
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event){
        if (event.phase == TickEvent.Phase.START && toggled){
            if (Minecraft.getMinecraft().playerController != null && Minecraft.getMinecraft().playerController.isInCreativeMode()) {
                return;
            }
            if (destroyingBlock) {
                destroyProgress += (float) (Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock().getPlayerRelativeBlockHardness(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().theWorld, blockPos) * speed);
                if (destroyProgress >= 1.0f) {
                    Minecraft.getMinecraft().theWorld.setBlockState(blockPos, Blocks.air.getBlockState().getBaseState(), 11);
                    Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, blockFacing));
                    destroyProgress = 0.0f;
                    destroyingBlock = false;
                }
            }

        }
    }

}
