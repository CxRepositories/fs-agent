package org.whitesource.agent.dependency.resolver.packageManger;

public enum LinuxPkgManagerCommand {

    DEBIAN_COMMAND("dpkg -l"),
    RPM_COMMAND("rpm -qa"),
    ALPINE_COMMAND("apk -vv info"),
    ARCH_LINUX_COMMAND("pacman -Q");

    private String command;

    LinuxPkgManagerCommand(String url) {
        this.command = url;
    }

    public String getCommand() {
        return command;
    }
}
