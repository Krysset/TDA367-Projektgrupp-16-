package com.g16.feyrune.model.combat;

import com.g16.feyrune.interfaces.ICombatCreature;
import com.g16.feyrune.model.combat.actions.IMove;

import com.g16.feyrune.Util.Random;

public class AttackHandler {

    /**
     * Used to make a monster attack another monster
     * @param attacker The monster performing the attack
     * @param defender The monster trying to evade the attack
     * @param attack The specific attack being used
     */
    public static void handleAttack(ICombatCreature attacker, ICombatCreature defender, IMove attack){
        if (evasiveManoeuvre(defender, attack)){
            int damage = calculateDamage(attacker, attack);
            defender.takeDamage(damage);
        }
    }

    /**
     * Calculates the damage done by an attack
     * @param attacker: creature containing the attack used in the action
     * @param attack: the attack action used against the defender
     * @return damage dealt to the defender (only calculates, doesn't deal it)
     */
    private static int calculateDamage(ICombatCreature attacker, IMove attack){
        int damage = attacker.getPower() * attack.getAttackPower();
        return damage;
    }

    /**
     * Calculates whether the attack accuracy is high enough to hit and whether the defender's evasiveness is high enough to evade it
     * @param defender: monster trying to evade the attack
     * @param baseAttack: attack to check accuracy on.
     * @return if attack is successful: true, if it misses false
     */
    private static boolean evasiveManoeuvre(ICombatCreature defender, IMove baseAttack){
        boolean evasion = defender.getSpeed()< Random.random.nextInt(100);
        boolean accuracy = baseAttack.getAttackAccuracy()>Random.random.nextInt(100);
        boolean hit= evasion && accuracy;
        return hit;
    }
}
