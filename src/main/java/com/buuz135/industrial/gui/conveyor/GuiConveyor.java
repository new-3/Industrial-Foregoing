/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2019, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.buuz135.industrial.gui.conveyor;

import com.buuz135.industrial.IndustrialForegoing;
import com.buuz135.industrial.api.conveyor.ConveyorUpgrade;
import com.buuz135.industrial.api.conveyor.gui.IGuiComponent;
import com.buuz135.industrial.gui.component.FilterGuiComponent;
import com.buuz135.industrial.proxy.block.filter.IFilter;
import com.buuz135.industrial.proxy.network.ConveyorButtonInteractMessage;
import com.buuz135.industrial.utils.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiConveyor extends ContainerScreen<ContainerConveyor> {

    public static final ResourceLocation BG_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/conveyor.png");

    private ConveyorUpgrade upgrade;
    private List<IGuiComponent> componentList;
    private int x;
    private int y;
    private List<IFilter.GhostSlot> ghostSlots;

    public GuiConveyor(ContainerConveyor inventorySlotsIn, PlayerInventory inventory, ITextComponent component) {
        super(inventorySlotsIn, inventory, component);
        this.upgrade = getContainer().getConveyor().getUpgradeMap().get(getContainer().getFacing());
        this.componentList = new ArrayList<>();
        this.ghostSlots = new ArrayList<>();
    }

    @Override
    protected void func_231160_c_() {
        super.func_231160_c_();
        componentList.clear();
        upgrade.addComponentsToGui(componentList);
        for (IGuiComponent iGuiComponent : componentList) {
            if (iGuiComponent instanceof FilterGuiComponent) {
                ghostSlots.addAll(Arrays.asList(((FilterGuiComponent) iGuiComponent).getFilter().getFilter()));
            }
        }
    }

    @Override
    protected void func_230450_a_(MatrixStack stack, float partialTicks, int mouseX, int mouseY) { //background
        this.func_238651_a_(stack, 0);
        RenderSystem.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(BG_TEXTURE);
        x = (field_230708_k_ - xSize) / 2;
        y = (field_230709_l_ - ySize) / 2;
        func_238474_b_(stack, x, y, 0, 0, xSize, ySize);
        if (upgrade != null) {
            String localized = new TranslationTextComponent(String.format("conveyor.upgrade.%s.%s", upgrade.getFactory().getRegistryName().getNamespace(), upgrade.getFactory().getRegistryName().getPath())).getString();
            getMinecraft().fontRenderer.func_238421_b_(stack, localized, x + xSize / 2 - getMinecraft().fontRenderer.getStringWidth(localized) / 2, y + 6, 0x404040);
        }
        for (IGuiComponent iGuiComponent : componentList) {
            iGuiComponent.drawGuiBackgroundLayer(stack, x, y, mouseX, mouseY);
        }
    }

    @Override
    protected void func_230451_b_(MatrixStack stack, int mouseX, int mouseY) { //foreground
        x = (field_230708_k_ - xSize) / 2;
        y = (field_230709_l_ - ySize) / 2;
        for (IGuiComponent iGuiComponent : componentList) {
            iGuiComponent.drawGuiForegroundLayer(stack, x, y, mouseX, mouseY);
        }
        func_230459_a_(stack, mouseX - x, mouseY - y);
        for (IGuiComponent iGuiComponent : componentList) {
            if (iGuiComponent.isInside(mouseX - x, mouseY - y)) {
                List<? extends ITextProperties> tooltips = iGuiComponent.getTooltip(x, y, mouseX, mouseY);
                if (tooltips != null) func_238654_b_(stack, tooltips, mouseX - x, mouseY - y);
            }
        }
    }

    public ContainerConveyor getContainer() {
        return (ContainerConveyor) super.getContainer();
    }

    @Override
    public boolean func_231048_c_(double mouseX, double mouseY, int mouseButton) { //mouseClicked
        boolean click = super.func_231048_c_(mouseX, mouseY, mouseButton);
        for (IGuiComponent iGuiComponent : componentList) {
            if (iGuiComponent.isInside(mouseX - x, mouseY - y)) {
                if (iGuiComponent.handleClick(this, x, y, mouseX, mouseY))
                    return true;
            }
        }
        return click;
    }

    public void sendMessage(int id, CompoundNBT compound) {
        IndustrialForegoing.NETWORK.get().sendToServer(new ConveyorButtonInteractMessage(upgrade.getPos(), id, upgrade.getSide(), compound));
    }

    public List<IFilter.GhostSlot> getGhostSlots() {
        return ghostSlots;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
