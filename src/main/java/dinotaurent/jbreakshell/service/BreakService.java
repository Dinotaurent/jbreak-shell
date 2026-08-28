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
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class BreakService {

    private final Terminal terminal;
    private Duration tiempoEnPc = Duration.ZERO;
    private int descansosCompletados = 0;
    private int descansosSaltados = 0;
    private int contadorDescansos = 0;
    private final String[] spinner = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};

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

//            count(2);
//            play(1);
//            notify("Momento de una pausa activa!!!");
            startBreak();

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

    private void count(int minutos) throws InterruptedException{
        TimeUnit.MINUTES.sleep(minutos);
    }

    private void play(int opcion){
        Resource recurso = opcion == 1 ? sonido1 : sonido2;

        try (
                // Se inicia el buffer para el recurso y se extrae el audio.
                BufferedInputStream buffer = new BufferedInputStream(recurso.getInputStream());
                AudioInputStream audio = AudioSystem.getAudioInputStream(buffer)
        ) {
            // Se crea un Clip y se le asigna el audio extraido del buffer
            Clip clip = AudioSystem.getClip();
            clip.open(audio);

            CountDownLatch latch = new CountDownLatch(1);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    latch.countDown();
                }
            });

            clip.start();
            latch.await();
            clip.close();

        } catch (Exception e) {
            IO.println("[sound] Error reproduciendo audio: " + e.getMessage());
        }
    }

    private void notify(String mensaje) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("linux")) {
                pb = new ProcessBuilder("notify-send", "JBreak-shell", mensaje);
            } else if (os.contains("windows")) {
                String toast = """
                [Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType=WindowsRuntime] | Out-Null
                $template = [Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent([Windows.UI.Notifications.ToastTemplateType]::ToastText02)
                $template.SelectSingleNode('//text[@id=1]').InnerText = '%s'
                $template.SelectSingleNode('//text[@id=2]').InnerText = '%s'
                $toast = [Windows.UI.Notifications.ToastNotification]::new($template)
                [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('JBreak').Show($toast)
                """.formatted("JBreak-shell", mensaje);
                pb = new ProcessBuilder("powershell", "-NoProfile", "-Command", toast);
            } else {
                return;
            }

            pb.start();

        } catch (Exception e) {
            IO.println("[notify] Error: " + e.getMessage());
        }
    }

    public void startBreak() throws InterruptedException {
        long segundos = TimeUnit.MINUTES.toSeconds(3);

        for (int i = 0; i < segundos ; i++) {
            String frame = spinner[i % spinner.length];
            terminal.writer().print(String.format("\r[%s] En pausa activa... ", frame));
            terminal.writer().flush();
            Thread.sleep(1000);
        }
        IO.println("pausa activa terminada.");

    }
}
