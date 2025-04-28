package org.ogsammaenr.conduitFlyv2.settings;

public class RankSettings {

    private final String permission;
    private final double radius;
    private final long duration;
    private final boolean preventFallDamage;
    private final int priority;

    /**************************************************************************************************************/
    //  constructor metodu
    public RankSettings(String permission, double radius, long duration, boolean preventFallDamage, int priority) {
        this.permission = permission;
        this.radius = radius;
        this.duration = duration;
        this.preventFallDamage = preventFallDamage;
        this.priority = priority;
    }

    /*=================== GETTERLAR =====================*/

    public String getPermission() {
        return permission;
    }

    public double getRadius() {
        return radius;
    }

    public long getDuration() {
        return duration;
    }

    public boolean shouldPreventFallDamage() {
        return preventFallDamage;
    }

    public int getPriority() {
        return priority;
    }
    /*=================================================*/
}
