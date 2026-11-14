package com.tridevmc.architecture.common.ui;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.client.ui.UISawbench;
import com.tridevmc.architecture.common.block.container.ContainerSawbench;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Lifted from DV - Should probably move this into compound as it makes things nice to work with...
 */
public class ArchitectureUIHooks {

    public static MenuType<ContainerSawbench> register(RegisterEvent.RegisterHelper<MenuType<?>> registry) {
        MenuType<ContainerSawbench> type = IMenuTypeExtension.create(getFactory());
        registry.register(Identifier.fromNamespaceAndPath(ArchitectureMod.MOD_ID, "containers"), type);
        return type;
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.<ContainerSawbench, UISawbench>register(ArchitectureMod.CONTENT.universalMenuType, UISawbench::new);
    }

    private static IContainerFactory<ContainerSawbench> getFactory() {
        return (windowId, inventory, data) -> {
            UIType type = UIType.byId(data.readByte());
            var context = new CreateMenuContext(windowId, inventory.player, inventory);
            var level = inventory.player.level();
            IElementProvider<?> provider;
            switch (type) {
                case TILE -> {
                    var pos = data.readBlockPos();
                    var state = level.getBlockState(pos);
                    var blockEntity = level.getBlockEntity(pos);
                    context.setPos(pos).setBlockState(state).setBlockEntity(blockEntity);
                    provider = blockEntity instanceof IElementProvider<?> element ? element
                            : state.getBlock() instanceof IElementProvider<?> element ? element : null;
                }
                case ENTITY -> {
                    var entity = level.getEntity(data.readVarInt());
                    context.setEntity(entity);
                    provider = entity instanceof IElementProvider<?> element ? element : null;
                }
                default -> throw new IllegalArgumentException("Unsupported ArchitectureCraft menu provider: " + type);
            }
            if (provider != null && provider.createMenu(context) instanceof ContainerSawbench menu) return menu;
            throw new IllegalStateException("ArchitectureCraft sawbench menu provider is unavailable");
        };
    }

    public static void openGui(Player player, IElementProvider<AbstractContainerMenu> provider) {
        if (player instanceof ServerPlayer) {
            if (provider instanceof BlockEntity be) {
                openGui((ServerPlayer) player, provider, be);
            } else if (provider instanceof Entity e) {
                openGui((ServerPlayer) player, provider, e);
            }
        } else {
            throw new ClassCastException(String.format("Unable to cast type %s to ServerPlayerEntity", player.getClass().getName()));
        }
    }

    public static void openGui(ServerPlayer player, IElementProvider<? extends AbstractContainerMenu> provider, BlockEntity blockEntity) {
        player.openMenu(new MenuProvider() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory inventory, @NotNull Player player) {
                return provider.createMenu(new CreateMenuContext(pContainerId, player, inventory).setBlockState(blockEntity.getBlockState()).setBlockEntity(blockEntity).setPos(blockEntity.getBlockPos()));
            }

            @Override
            public @NotNull Component getDisplayName() {
                return provider.getDisplayName();
            }
        }, buf -> {
            buf.writeByte(UIType.TILE.getId());
            buf.writeBlockPos(blockEntity.getBlockPos());
        });
    }

    public static void openGui(ServerPlayer player, IElementProvider<? extends AbstractContainerMenu> provider, BlockPos pos) {
        player.openMenu(new MenuProvider() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory inventory, @NotNull Player player) {
                return provider.createMenu(new CreateMenuContext(pContainerId, player, inventory).setPos(pos));
            }

            @Override
            public @NotNull Component getDisplayName() {
                return provider.getDisplayName();
            }
        }, buf -> {
            buf.writeByte(UIType.TILE.getId());
            buf.writeBlockPos(pos);
        });
    }

    public static void openGui(ServerPlayer player, IElementProvider<? extends AbstractContainerMenu> provider, Entity entity) {
        player.openMenu(new MenuProvider() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory inventory, @NotNull Player player) {
                return provider.createMenu(new CreateMenuContext(pContainerId, player, inventory).setEntity(entity));
            }

            @Override
            public @NotNull Component getDisplayName() {
                return provider.getDisplayName();
            }
        }, buf -> {
            buf.writeByte(UIType.ENTITY.getId());
            buf.writeVarInt(entity.getId());
        });
    }

    private enum UIType {
        TILE(0), ENTITY(1), OTHER(2);

        private static final UIType[] TYPES;

        static {
            TYPES = new UIType[]{TILE, ENTITY, OTHER};
        }

        private final int id;

        UIType(int id) {
            this.id = id;
        }

        public static UIType byId(int id) {
            if (id < 0 || id >= TYPES.length) throw new IllegalArgumentException("Unknown menu provider: " + id);
            return TYPES[id];
        }

        public int getId() {
            return this.id;
        }
    }

}
