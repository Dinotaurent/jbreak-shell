package dinotaurent.jbreakshell.service;

import dinotaurent.jbreakshell.dto.BreakStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BreakService {

    private int descansosCompletados = 0;
    private int descansosSaltados = 0;
    private int contadorDescansos = 0;

    public Optional<BreakStatus> start(){
        IO.println("Se ejecuta metodo start()");
        descansosCompletados++;
        descansosSaltados++;
        contadorDescansos++;

        return Optional.of(getInfo());
    }

    public BreakStatus getInfo(){
        return new BreakStatus(
                this.descansosCompletados,
                this.descansosSaltados,
                this.contadorDescansos
        );
    }
}
