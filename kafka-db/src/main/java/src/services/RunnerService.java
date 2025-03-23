package src.services;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import src.bdCreator.IcebergTableCreator;


@Component
public class RunnerService implements CommandLineRunner {

    private final IcebergTableCreator icebergTableService;

    public RunnerService(IcebergTableCreator icebergTableService) {
        this.icebergTableService = icebergTableService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("IcebergTablesInitializer started");
        icebergTableService.createTablesIfNotExist();
        System.out.println("IcebergTablesInitializer finished");
    }
}
