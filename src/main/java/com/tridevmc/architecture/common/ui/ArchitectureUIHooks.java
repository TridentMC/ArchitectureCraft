package com.tridevmc.architecture.common.ui;

import com.tridevmc.architecture.common.ArchitectureMod;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Optional;

/**
 * Lifted from DV - Should probably move this into compound as it makes things nice to work with...
 */
public class ArchitectureUIHooks {

    private static Optional<IElementProvider> lastProvider = Optional.empty();

    public static <C extends Container> ContainerType<C> register(IForgeRegistry<ContainerType<?>> registry) {
        ContainerType<C> containerType = IForgeContainerType.create(getFactory());
        containerType.setRegistryName(ArchitectureMod.MOD_ID, "containers");
        registry.register(containerType);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ScreenManager.registerFactory(containerType, getScreenFactory()));
        return containerType;
    }

    private static <C extends Container> IContainerFactory<C> getFactory() {
        return (windowId, inv, data) -> {
            UIType type = UIType.byId(data.readByte());
            CreateMenuContext context = new CreateMenuContext(windowId, inv.player, inv);
            World world = inv.player.world;

            switch (type) {
                case TILE:
                    BlockPos pos = data.readBlockPos();
                    BlockState state = world.getBlockState(pos);
                    TileEntity tile = world.getTileEntity(pos);
                    context.setPos(pos).setBlockState(state).setBlockEntity(tile);
                    if (tile instanceof IElementProvider) {
                        lastProvider = Optional.of((IElementProvider) tile);
                        return (C) ((IElementProvider) tile).createMenu(context);
                    } else if (state.getBlock() instanceof IElementProvider) {
                        lastProvider = Optional.of((IElementProvider) state.getBlock());
                        return (C) ((IElementProvider) state.getBlock()).createMenu(context);
                    }
                case ENTITY:
                    int entityId = data.readVarInt();
                    Entity entity = world.getEntityByID(entityId);
                    context.setEntity(entity);
                    if (entity instanceof IElementProvider) {
                        lastProvider = Optional.of((IElementProvider) entity);
                        return (C) ((IElementProvider) entity).createMenu(context);
                    }
                default:
                    lastProvider = Optional.empty();
                    return null;
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    private static ScreenManager.IScreenFactory getScreenFactory() {
        return (container, inv, name) -> lastProvider.map(eP -> eP.createScreen(container, inv.player)).orElse(null);
    }

    public static void openGui(PlayerEntity player, IElementProvider provider) {
        if (player instanceof ServerPlayerEntity) {
            if (provider instanceof TileEntity) {
                openGui((ServerPlayerEntity) player, provider, ((TileEntity) provider).getPos());
            } else if (provider instanceof Entity) {
                openGui((ServerPlayerEntity) player, provider, ((Entity) provider).getEntityId());
            }
        } else {
            throw new ClassCastException(String.format("Unable to cast type %s to ServerPlayerEntity", player.getClass().getName()));
        }
    }

    public static void openGui(ServerPlayerEntity player, IElementProvider provider, BlockPos pos) {
        NetworkHooks.openGui(player, provider, packetBuffer -> {
            packetBuffer.writeByte(UIType.TILE.id);
            packetBuffer.writeBlockPos(pos);
        });
    }

    public static void openGui(ServerPlayerEntity player, IElementProvider provider, int entity) {
        NetworkHooks.openGui(player, provider, packetBuffer -> {
            packetBuffer.writeByte(UIType.ENTITY.id);
            packetBuffer.writeVarInt(entity);
        });
    }

    private enum UIType {
        TILE(0),
        ENTITY(1),
        OTHER(2);

        private static final UIType[] TYPES;

        static {
            TYPES = new UIType[]{TILE, ENTITY, OTHER};
        }

        private final int id;

        UIType(int id) {
            this.id = id;
        }

        public static UIType byId(int id) {
            return TYPES[id];
        }

        public int getId() {
            return this.id;
        }
    }

}
