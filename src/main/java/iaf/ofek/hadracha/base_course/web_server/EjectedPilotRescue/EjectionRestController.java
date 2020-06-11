package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;


import iaf.ofek.hadracha.base_course.web_server.Data.InMemoryMapDataBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ejectedPilotRescue")
public class EjectionRestController {


    InMemoryMapDataBase myDataBase;


    public EjectionRestController(@Autowired InMemoryMapDataBase myDataBase) {
        this.myDataBase = myDataBase;
    }

    @GetMapping("/infos")
    public List<EjectedPilotInfo> sendAllEjectionToClient(){
        return myDataBase.getAllOfType(EjectedPilotInfo.class);
    }
}
