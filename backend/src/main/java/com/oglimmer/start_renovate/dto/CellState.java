/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.dto;

/**
 * One cell in the comparison matrix: how a single Renovate option is configured in one repo.
 *
 * <ul>
 *   <li>{@code SET_ON} — the option is present and enabled ({@code value} carries the concrete
 *       value for enums/strings; null for plain booleans).
 *   <li>{@code SET_OFF} — determinably off / absent for a preset-or-rule-driven option.
 *   <li>{@code UNSET} — not configured; falls back to Renovate's own default.
 *   <li>{@code CUSTOM} — present in a shape we cannot confidently reverse-map ({@code value} may
 *       carry the raw representation).
 * </ul>
 */
public record CellState(String state, String value) {

  public static final String SET_ON = "SET_ON";
  public static final String SET_OFF = "SET_OFF";
  public static final String UNSET = "UNSET";
  public static final String CUSTOM = "CUSTOM";

  public static CellState on() {
    return new CellState(SET_ON, null);
  }

  public static CellState on(String value) {
    return new CellState(SET_ON, value);
  }

  public static CellState off() {
    return new CellState(SET_OFF, null);
  }

  public static CellState unset() {
    return new CellState(UNSET, null);
  }

  public static CellState custom(String value) {
    return new CellState(CUSTOM, value);
  }

  /** Convenience for preset/rule booleans: present ⇒ SET_ON, absent ⇒ SET_OFF. */
  public static CellState bool(boolean present) {
    return present ? on() : off();
  }
}
