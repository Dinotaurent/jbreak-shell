package dinotaurent.jbreakshell.command;

import dinotaurent.jbreakshell.dto.BreakStatus;
import dinotaurent.jbreakshell.service.BreakService;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BreakCommands {

    private static final boolean IS_WINDOWS = System.getProperty("os.name")
            .toLowerCase()
            .contains("win");

    private final BreakService service;

    public BreakCommands(BreakService service) {
        this.service = service;
    }

    @Command(name = "start", description = "Inicia la sesión")
    public void start(){

        while(true){
            Optional<BreakStatus> result = service.start();

            if (result.isEmpty()){
                IO.println("\uD83D\uDC4B JBreak-shell detenido. ¡Recuerda hacer pausas hoy también!");
                break;
            }
            IO.println(info());
        }
    }

    @Command(name = "info", description = "Muestra la informacion de la sesión")
    public String info(){
        return formatStatusOutput(service.getInfo());
    }


    private String formatStatusOutput(BreakStatus status) {
        if (IS_WINDOWS) {
            return buildWindowsOutput(status);
        }
        return buildUnixOutput(status);
    }

    private String buildUnixOutput(BreakStatus status) {
        return String.format("""
            ╭───────────────────────────────────────────────────╮
            │                 🚀 ESTADO DE SESIÓN               │
            ├───────────────────────────────────────────────────┤
            │  ⏱️  Tiempo en PC          │  %-20s │
            │  🔁 Ciclo de descansos    │  %-20s │
            │  ✅ Tomados en sesión     │  %-20d │
            │  ⏭️  Saltados en sesión    │  %-20d │
            ╰───────────────────────────────────────────────────╯
            """,
                status.tiempoEnPcFormat(),
                status.contadorDescansos() + " de 3",
                status.descansosCompletados(),
                status.descansosSaltados()
        );
    }

    private String buildWindowsOutput(BreakStatus status) {
        return String.format("""
            +---------------------------------------------------+
            |               🚀 METRICAS DE PAUSA                 |
            +-----------------------------------------+---------+
            | [⏳] Tiempo en PC                        | %-7s |
            | [🔄] Progreso del ciclo                  | %d/3     |
            | [🧘] Descansos completados               | %-7d |
            | [⏭️] Descansos omitidos                  | %-7d |
            +-----------------------------------------+---------+
            """,
                status.tiempoEnPcFormat(),
                status.contadorDescansos(),
                status.descansosCompletados(),
                status.descansosSaltados()
        );
    }
}
