package desafio.gustavo.services;

import desafio.gustavo.dto.EventDTO;
import desafio.gustavo.entities.City;
import desafio.gustavo.entities.Event;
import desafio.gustavo.repositories.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Page<EventDTO> findAllPaged(Pageable pageable) {
        Page<Event> list = eventRepository.findAll(pageable);
        return list.map(EventDTO::new);
    }

    @Transactional
    public EventDTO insert(EventDTO dto) {
        var event = new Event();
        event.setName(dto.getName());
        event.setDate(dto.getDate());
        event.setUrl(dto.getUrl());
        event.setCity(new City(dto.getCityId(), dto.getName()));
        event = eventRepository.save(event);
        return new EventDTO(event);
    }
}
