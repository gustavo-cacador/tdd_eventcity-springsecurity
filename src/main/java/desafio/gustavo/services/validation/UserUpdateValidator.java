package desafio.gustavo.services.validation;

import desafio.gustavo.controllers.exceptions.FieldMessage;
import desafio.gustavo.dto.UserUpdateDTO;
import desafio.gustavo.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.servlet.HandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {

    private final HttpServletRequest httpServletRequest;

    private final UserRepository userRepository;

    public UserUpdateValidator(HttpServletRequest httpServletRequest, UserRepository userRepository) {
        this.httpServletRequest = httpServletRequest;
        this.userRepository = userRepository;
    }

    @Override
    public void initialize(UserUpdateValid ann) {
    }

    @Override
    public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {

        @SuppressWarnings("unchecked")
        var uri = (Map<String, String>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        long userId = Long.parseLong(uri.get("id"));

        List<FieldMessage> list = new ArrayList<>();

        var user = userRepository.findByEmail(dto.getEmail());

        // se email já existir, ele retorna uma mensagem de erro
        // se o id do usuario nao for o id do usuario que eu quero atualizar (tentando atualizar o msm email de outro usuario que ja existe) , ele retorna uma mensagem de erro
        // a lógica n permite eu atualizar um email no qual já existe (de algum usuario já existente)
        if (user != null && userId != user.getId()) {
            list.add(new FieldMessage("email", "Email já existe!"));
        }

        for (FieldMessage e : list) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
                    .addConstraintViolation();
        }
        return list.isEmpty();
    }
}
