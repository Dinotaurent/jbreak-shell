package dinotaurent.jbreakshell.service;

import dinotaurent.jbreakshell.dto.BreakStatus;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.SignalHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class BreakService {

    private final Terminal terminal;
    private Duration tiempoEnPc = Duration.ZERO;
    private int descansosCompletados = 0;
    private int descansosSaltados = 0;
    private int contadorDescansos = 0;

    @Value("classpath:alert.wav")
    private Resource sonido1;

    @Value("classpath:alert2.wav")
    private Resource sonido2;

    // Hilo actual, guardarlo es necesario para poderlo interrumpirlo.
    private volatile Thread timerThread = null;

    public BreakService(Terminal terminal) {
        this.terminal = terminal;
    }

    public Optional<BreakStatus> start(){
        IO.println("Se ejecuta metodo start()");

        // Guardar handler anterior y crear uno nuevo temporal para CTRL + C
        SignalHandler prevHandler =
                terminal.handle(Terminal.Signal.INT, signal -> {
                    IO.println("\n[DEBUG] Se recibió CTRL + C");
                    Thread hilo = timerThread;

                    if (hilo != null) {
                        hilo.interrupt();
                    }
                });

        // Guardar atributos de la terminal original de spring shell.
        Attributes configTerminalOriginal = terminal.getAttributes();
        // Crear nuevos atributos custom temporales.
        Attributes configTerminalTemp = new Attributes(configTerminalOriginal);

        // Configuracion para evitar que salga la entrada en la consola.
        configTerminalTemp.getLocalFlags().remove(Attributes.LocalFlag.ECHO);

        // Aplicar los atributos custom para que no se vea la entrada en la consola a la terminal.
        terminal.setAttributes(configTerminalTemp);

        // Guarda el hilo actual.
        timerThread = Thread.currentThread();

        try {
            IO.println("[▶] Temporizador de pausa activa!");
            IO.println("[i] Presiona CTRL + C en cualquier momento para cancelar.");

            contar(2);
            play(1);

            // --- En construccion ---
            tiempoEnPc = tiempoEnPc.plus(Duration.ofMinutes(30));
            descansosCompletados++;
            descansosSaltados++;
            contadorDescansos++;

            return Optional.of(getInfo());
            // ---
        } catch (InterruptedException e){
            IO.println("\n[✖] Temporizador cancelado.");
            play(2);
            return Optional.empty();
        } finally {
            // Se limpia la referencia del hilo al terminar correctamente o interrumpirse.
            timerThread = null;

            // Restaura la config de la terminal original.
            terminal.setAttributes(configTerminalOriginal);

            // Restaura el handler original de spring shell.
            if (prevHandler != null) {
                terminal.handle(Terminal.Signal.INT, prevHandler);
            }
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
        TimeUnit.MINUTES.sleep(minutos);
    }

    private void play(int opcion){
        BufferedInputStream reader;
        try {
            if (opcion == 1){
                reader = new BufferedInputStream(sonido1.getInputStream());
            } else {
                reader = new BufferedInputStream(sonido2.getInputStream());
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(reader);

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

        } catch (Exception e){
            IO.println("Se produjo un error: " + e.getMessage());
        }
    }
}
