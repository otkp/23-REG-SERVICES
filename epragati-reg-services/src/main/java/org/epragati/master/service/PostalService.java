package org.epragati.master.service;

import java.util.List;

import org.epragati.master.dto.PostOfficeDTO;
import org.epragati.master.vo.PostOfficeVO;

public interface PostalService {

	List<PostOfficeVO> findByDidtictId(Integer district);
}
