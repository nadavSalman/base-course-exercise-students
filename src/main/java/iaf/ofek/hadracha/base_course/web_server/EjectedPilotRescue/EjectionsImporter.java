package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;

import iaf.ofek.hadracha.base_course.web_server.Data.CrudDataBase;
import iaf.ofek.hadracha.base_course.web_server.Data.Entity;
import iaf.ofek.hadracha.base_course.web_server.Utilities.ListOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class EjectionsImporter {

    @Value("${ejections.server.url}")
    public String EJECTION_SERVER_URL;
    @Value("${ejections.namespace}")
    public String NAMESPACE;





    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final RestTemplate restTemplate;
    private final CrudDataBase dataBase;
    private final ListOperations listOperations;
    private static final Double SHIFT_NORTH = 1.7;

    public EjectionsImporter(RestTemplateBuilder restTemplateBuilder, CrudDataBase dataBase, ListOperations listOperations) {
        restTemplate = restTemplateBuilder.build();
        this.dataBase = dataBase;
        this.listOperations = listOperations;
        executor.scheduleAtFixedRate(this::updateEjections, 1, 1, TimeUnit.SECONDS);
    }

    private List<EjectedPilotInfo> mikaRequest(){
        try {
            ResponseEntity<List<EjectedPilotInfo>> responseEntity = restTemplate.exchange(
                    EJECTION_SERVER_URL + "/ejections?name=" + NAMESPACE, HttpMethod.GET,
                    null, new ParameterizedTypeReference<List<EjectedPilotInfo>>() {
                    });

            return responseEntity.getBody();

        }catch (RestClientException e) {
            System.err.println("Requesr from mika fail : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void AccuracyOfPilotLocationAfterEjection(List<EjectedPilotInfo> ejectionsFromServer){
        if (ejectionsFromServer != null) {
            for(EjectedPilotInfo ejectedPilotInfo: ejectionsFromServer) {
                ejectedPilotInfo.getCoordinates().lat += SHIFT_NORTH;
            }
        }
    }

    private void ejectionData(List<EjectedPilotInfo> ejectionsFromServer){
        List<EjectedPilotInfo> updatedEjections = ejectionsFromServer;
        List<EjectedPilotInfo> previousEjections = dataBase.getAllOfType(EjectedPilotInfo.class);

        List<EjectedPilotInfo> addedEjections = ejectionDifferance(updatedEjections, previousEjections);
        List<EjectedPilotInfo> removedEjections = ejectionDifferance(updatedEjections, previousEjections);

        addedEjections.forEach(dataBase::create);
        removedEjections.stream().map(EjectedPilotInfo::getId).forEach(id -> dataBase.delete(id, EjectedPilotInfo.class));
    }
    private void updateEjections() {
        try {
            List<EjectedPilotInfo> ejectionsFromServer = mikaRequest();

            AccuracyOfPilotLocationAfterEjection(ejectionsFromServer);


            ejectionData(ejectionsFromServer);

        } catch (RestClientException e) {
            System.err.println("Could not insert data: " + e.getMessage());
            e.printStackTrace();
        }
    }




    private List<EjectedPilotInfo> ejectionDifferance (List<EjectedPilotInfo> updatedEjections, List<EjectedPilotInfo> previousEjections) {
        return listOperations.subtract(updatedEjections, previousEjections, new Entity.ByIdEqualizer<>());
    }


}
