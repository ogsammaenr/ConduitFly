package org.ogsammaenr.conduitFly.settings;

public class RankSettings {

    private final String permission;
    private final double radius;
    private final long duration;
    private final boolean preventFallDamage;
    private final int priority;
    private final double price;
    private final String displayName;

    /**************************************************************************************************************/
    //  constructor metodu
    public RankSettings(String permission, double radius, long duration, boolean preventFallDamage, double price, String displayName, int priority) {
        this.permission = permission;
        this.radius = radius;
        this.duration = duration;
        this.preventFallDamage = preventFallDamage;
        this.priority = priority;
        this.price = price;
        this.displayName = displayName;
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

    public double getPrice() {
        return price;
    }

    public String getDisplayName() {
        return displayName;
    }
    /*=================================================*/
}
