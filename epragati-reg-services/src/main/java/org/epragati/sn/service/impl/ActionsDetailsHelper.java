package org.epragati.sn.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.epragati.sn.dto.ActionsDetails;
import org.epragati.sn.dto.SpecialNumberDetailsDTO;
import org.epragati.sn.numberseries.dto.PRNumberSeriesConfigDTO;
import org.epragati.sn.numberseries.dto.PRPoolDTO;
import org.springframework.stereotype.Component;

@Component
public class ActionsDetailsHelper {

	public void updateActionsDetails(SpecialNumberDetailsDTO entity, String user) {

		updateActionsDetails(entity, user, entity.getBidStatus().getDescription(),
				entity.getBidStatus().getDescription());

	}

	public void updateActionsDetails(SpecialNumberDetailsDTO entity, String user, String reason, String action) {
		ActionsDetails actionDetailsDTO = new ActionsDetails();

		actionDetailsDTO.setActionBy(user);
		actionDetailsDTO.setActionDatetime(LocalDateTime.now());
		actionDetailsDTO.setReason(reason);
		actionDetailsDTO.setAction(action);

		if (entity.getActionsDetailsLog() == null) {
			entity.setActionsDetailsLog(new ArrayList<>());
		}
		entity.getActionsDetailsLog().add(actionDetailsDTO);
	}

	public void updateActionsDetails(PRPoolDTO entity, String user) {
		ActionsDetails actionDetailsDTO = new ActionsDetails();

		actionDetailsDTO.setActionBy(user);
		actionDetailsDTO.setActionDatetime(LocalDateTime.now());
		actionDetailsDTO.setReason(entity.getPoolStatus().getDescription());
		actionDetailsDTO.setAction(entity.getPoolStatus().getDescription());

		if (entity.getActionDetailsLog() == null) {
			entity.setActionDetailsLog(new ArrayList<>());
		}
		entity.getActionDetailsLog().add(actionDetailsDTO);
	}

	public void updateActionsDetails(PRNumberSeriesConfigDTO entity, String user, String reason, String action) {
		ActionsDetails actionDetailsDTO = new ActionsDetails();

		actionDetailsDTO.setActionBy(user);
		actionDetailsDTO.setActionDatetime(LocalDateTime.now());
		actionDetailsDTO.setReason(reason);
		actionDetailsDTO.setAction(action);

		if (entity.getActionDetailsLog() == null) {
			entity.setActionDetailsLog(new ArrayList<>());
		}
		entity.getActionDetailsLog().add(actionDetailsDTO);
	}

}
