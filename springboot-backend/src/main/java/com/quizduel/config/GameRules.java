package com.quizduel.config;

/** Game constants. Change them here. */
public final class GameRules {
    private GameRules() {}

    public static final int WIN_POINTS = 50;           // winning against a classmate
    public static final int BOT_WIN_POINTS = 25;       // winning against the Practice Bot
    public static final int LOSS_POINTS = 20;          // losing to a classmate (points never drop below 0)
    public static final int BOT_LOSS_POINTS = 10;      // losing to the Practice Bot
    public static final int QUESTIONS_PER_BATTLE = 10;
}
