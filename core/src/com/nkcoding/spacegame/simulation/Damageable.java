package com.nkcoding.spacegame.simulation;

import com.badlogic.gdx.physics.box2d.Fixture;

public interface Damageable {
    boolean damageAt(Fixture fixture, int damage);
}
