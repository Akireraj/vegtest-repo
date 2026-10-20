package ar.edu.vetgest.util;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * HERENCIA de una clase abstracta del JDK: java.time.Clock. Avanza como el
 * reloj del sistema pero admite un desfase, de modo que el escenario de
 * demostracion puede "adelantarse" al dia del turno y las pruebas son
 * reproducibles. Los servicios reciben un Clock y no saben si es real o simulado.
 */
public class RelojSimulado extends Clock {

    private final Clock base;
    private Duration desfase = Duration.ZERO;

    public RelojSimulado(Clock base) {
        this.base = base;
    }

    public static RelojSimulado delSistema() {
        return new RelojSimulado(Clock.systemDefaultZone());
    }

    /** Ajusta el reloj para que "ahora" sea el momento indicado; luego sigue avanzando. */
    public void ajustarA(LocalDateTime momento) {
        desfase = Duration.between(LocalDateTime.now(base), momento);
    }

    @Override
    public ZoneId getZone() {
        return base.getZone();
    }

    @Override
    public Clock withZone(ZoneId zona) {
        RelojSimulado otro = new RelojSimulado(base.withZone(zona));
        otro.desfase = this.desfase;
        return otro;
    }

    @Override
    public Instant instant() {
        return base.instant().plus(desfase);
    }
}
