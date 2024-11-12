package org.cardano.foundation.lob;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.command.annotation.EnableCommand;

@SpringBootApplication
@ComponentScan(basePackages = {
		"org.cardano.foundation.lob.service",
		"org.cardano.foundation.lob.config",
		"org.cardano.foundation.lob.shell"
})
@Slf4j
@EnableCommand
public class CFLOBTxSubmitter {

    public static void main(String[] args) {
		SpringApplication.run(CFLOBTxSubmitter.class, args);
	}

}
