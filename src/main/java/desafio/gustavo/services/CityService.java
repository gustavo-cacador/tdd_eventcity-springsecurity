package desafio.gustavo.services;

import desafio.gustavo.dto.CityDTO;
import desafio.gustavo.entities.City;
import desafio.gustavo.repositories.CityRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Transactional
    public List<CityDTO> findAll() {
        List<City> list = cityRepository.findAll(Sort.by("name"));
        return list.stream().map(CityDTO::new).toList();
    }

    @Transactional
    public CityDTO insert(CityDTO dto) {
        var city = new City();
        city.setName(dto.getName());
        city = cityRepository.save(city);
        return new CityDTO(city);
    }
}
