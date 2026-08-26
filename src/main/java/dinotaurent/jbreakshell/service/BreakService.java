package dinotaurent.jbreakshell.service;

import dinotaurent.jbreakshell.dto.BreakStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class BreakService {

    private Duration tiempoEnPc = Duration.ZERO;
    private int descansosCompletados = 0;
    private int descansosSaltados = 0;
    private int contadorDescansos = 0;

    public Optional<BreakStatus> start(){
        IO.println("Se ejecuta metodo start()");

        try {
            IO.println("[▶] Temporizador de pausa activa!");
            IO.println("[i] Presiona CTRL + C en cualquier momento para cancelar.");
            contar(2);

            tiempoEnPc = tiempoEnPc.plus(Duration.ofMinutes(30));
            descansosCompletados++;
            descansosSaltados++;
            contadorDescansos++;

            return Optional.of(getInfo());
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            IO.println("Se ha producido un error");
            return Optional.empty();
        }


    }

    public BreakStatus getInfo(){
        return new BreakStatus(
                this.tiempoEnPc,
                this.descansosCompletados,
                this.descansosSaltados,
                this.contadorDescansos
        );
    }

    private void contar(int minutos) throws InterruptedException{
        long totalSegundos = TimeUnit.MINUTES.toSeconds(minutos);

        for (long segundoActual = 1; segundoActual<=totalSegundos; segundoActual++) {
            Thread.sleep(1000);
        }
    }
}
