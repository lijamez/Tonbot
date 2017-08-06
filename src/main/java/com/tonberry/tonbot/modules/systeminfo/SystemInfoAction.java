package com.tonberry.tonbot.modules.systeminfo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.BotUtils;
import com.tonberry.tonbot.common.MessageReceivedAction;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.List;

public class SystemInfoAction implements MessageReceivedAction {

    private static final List<String> ROUTE = ImmutableList.of("systeminfo");

    private final SystemInfo systemInfo;

    @Inject
    public SystemInfoAction(SystemInfo systemInfo) {
        this.systemInfo = Preconditions.checkNotNull(systemInfo, "systemInfo must be non-null.");
    }

    @Override
    public List<String> getRoute() {
        return ROUTE;
    }

    @Override
    public void enact(MessageReceivedEvent event, String args) {

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.withTitle("System Information");

        ComputerSystem cs = systemInfo.getHardware().getComputerSystem();
        embedBuilder.appendField("Manufacturer", cs.getManufacturer(), true);
        embedBuilder.appendField("Model", cs.getModel(), true);

        OperatingSystem os = systemInfo.getOperatingSystem();
        embedBuilder.appendField("Operating System", os.getFamily() + " " + os.getVersion(), true);

        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        embedBuilder.appendField("Processor", processor.getName(), true);

        GlobalMemory mem = systemInfo.getHardware().getMemory();
        long memUsedMb = (mem.getTotal() - mem.getAvailable()) / 1000000;
        long memTotalMb = mem.getTotal() / 1000000;
        int memUsedPercent = (int) (((double) memUsedMb/memTotalMb) * 100);
        embedBuilder.appendField("Memory Usage", memUsedMb + " / " + memTotalMb + " MB (" + memUsedPercent + "%)", true);

        int cpuLoadPercent = (int) (processor.getSystemCpuLoad() * 100);
        System.out.println(processor.getSystemCpuLoad());
        embedBuilder.appendField("CPU Load", Integer.toString(cpuLoadPercent) + "%", true);

        BotUtils.sendEmbeddedContent(event.getChannel(), embedBuilder.build());
    }
}
