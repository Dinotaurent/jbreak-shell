package dinotaurent.jbreakshell.command;

import dinotaurent.jbreakshell.dto.BreakStatus;
import dinotaurent.jbreakshell.service.BreakService;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BreakCommands {

    private final BreakService service;

    public BreakCommands(BreakService service) {
        this.service = service;
    }

    @Command(name = "start", description = "Inicia la sesión")
    public void start(){

        while(true){
            Optional<BreakStatus> result = service.start();

            if (result.isEmpty()){
                IO.println("\n[!] Sesión de pausas activas finalizada.");
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
        return String.format("""
            +-----------------------------------------+
            |               ESTADO DE PAUSAS          |
            +-----------------------------------------+
            | Tiempo en PC:        %s            |
            | Progreso del ciclo:  %d de 3 descansos   |
            | Tomados en sesión:   %d                  |
            | Saltados en sesión:  %d                  |
            +-----------------------------------------+
            """,
                status.tiempoEnPcFormat(),
                status.contadorDescansos(),
                status.descansosCompletados(),
                status.descansosSaltados()
        );
    }
}
