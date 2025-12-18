package com.axalotl.async.fabric.mixin.server;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.dedicated.ServerWatchdog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(ServerWatchdog.class)
public class ServerWatchdogMixin {

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/CrashReport;addCategory(Ljava/lang/String;)Lnet/minecraft/CrashReportCategory;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void addCustomCrashReport(CallbackInfo ci, long i, long j, long k, CrashReport crashreport) {
        crashreport.addCategory("Async thread dump")
                .setDetail("All Threads", () -> {
                    StringBuilder sb = new StringBuilder();
                    Thread.getAllStackTraces().forEach((thread, stackTrace) -> {
                        sb.append(String.format("\"%s\" [%s]%n", thread.getName(), thread.getState()));
                        for (StackTraceElement ste : stackTrace) {
                            sb.append("\tat ").append(ste).append("\n");
                        }
                        sb.append("\n");
                    });
                    return sb.toString();
                });
    }
}