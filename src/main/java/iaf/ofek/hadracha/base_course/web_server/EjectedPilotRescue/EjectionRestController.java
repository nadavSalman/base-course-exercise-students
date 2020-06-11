package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;


import iaf.ofek.hadracha.base_course.web_server.Data.InMemoryMapDataBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ejectedPilotRescue")
public class EjectionRestController {


    InMemoryMapDataBase myDataBase;


    @Autowired
    AirplanesAllocationManager airplanesAllocationManager;


    public EjectionRestController(@Autowired InMemoryMapDataBase myDataBase) {
        this.myDataBase = myDataBase;
    }

    @GetMapping("/infos")
    public List<EjectedPilotInfo> sendAllEjectionToClient(){
        return myDataBase.getAllOfType(EjectedPilotInfo.class);
    }


    @GetMapping("/takeResponsibility")
    public void TakeResponsibility(@RequestParam int ejectionId, @CookieValue(value = "client-id", defaultValue = "") String clientId){
        EjectedPilotInfo ejectedPilotInfo = myDataBase.getByID(ejectionId, EjectedPilotInfo.class);

        if(ejectedPilotInfo == null){

        }

        if (ejectedPilotInfo.getRescuedBy() == null){
            ejectedPilotInfo.setRescuedBy(clientId);
            myDataBase.update(ejectedPilotInfo);
            airplanesAllocationManager.allocateAirplanesForEjection(ejectedPilotInfo, clientId);//set ejected point to green.
        }



    }




}
