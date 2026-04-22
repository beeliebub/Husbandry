package me.beeliebub.husbandry.trait;

/**
 * Controls how a trait is passed from parents to offspring during breeding.
 *
 * <ul>
 *   <li>{@link #DOMINANT} - Passed if at least ONE parent has it.</li>
 *   <li>{@link #RECESSIVE} - Passed only if BOTH parents have it.</li>
 *   <li>{@link #SPECIAL} - Never passed through breeding.</li>
 * </ul>
 */
public enum InheritanceType {
    DOMINANT,
    RECESSIVE,
    SPECIAL
}