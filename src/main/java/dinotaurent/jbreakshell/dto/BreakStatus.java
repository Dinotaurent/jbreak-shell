package dinotaurent.jbreakshell.dto;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

public record BreakStatus(
        Duration tiempoEnPc,
        int descansosCompletados,
        int descansosSaltados,
        int contadorDescansos
) {
    public String tiempoEnPcFormat() {
        long horas = tiempoEnPc.toHours();
        long minutos = tiempoEnPc.toMinutesPart();

        return "%02dH:%02dM".formatted(horas, minutos);
    }
}
