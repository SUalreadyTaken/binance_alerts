package com.su.Service;

import java.util.List;

import com.su.Model.Idle;
import com.su.Repository.IdleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IdleService {

	private final IdleRepository idleRepository;

	public IdleService(IdleRepository idleRepository) {
		this.idleRepository = idleRepository;
	}

	public boolean getAlternativeBoolean() {
		return idleRepository.findAll().get(0).isAlternative();
	}

	public void switchAlternativeBoolean() {
		List<Idle> idleList = idleRepository.findAll();
    if (!idleList.isEmpty()) {
      Idle currentIdle = idleList.get(0);
      currentIdle.setAlternative(!currentIdle.isAlternative());
      idleRepository.save(currentIdle);
    } else {
      System.err.println("No idle record in db.. insert 1 if u want switch to work");
    }
	}
}
