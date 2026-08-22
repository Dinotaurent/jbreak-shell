package dinotaurent.jbreakshell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.core.command.annotation.Command;

@SpringBootApplication
public class JbreakShellApplication {

    static void main(String[] args) {
        SpringApplication.run(JbreakShellApplication.class, args);
    }

    @Command(name = "saludar", description = "Da un saludo")
    public String saludar(){
        return "Hola mundo!";
    }

}
