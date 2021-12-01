package br.edu.unoesc.fdj.coronapp.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.edu.unoesc.fdj.coronapp.model.Paciente;
import br.edu.unoesc.fdj.coronapp.model.Sintoma;
import br.edu.unoesc.fdj.coronapp.service.PacienteService;

@Controller
public class PacienteController {
	@Autowired
	private PacienteService pacienteService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true, 10));
	}

	@RequestMapping("/buscar")
	public String buscar(@RequestParam(required = false) Long cpf, HttpSession session, Model model) {
		if (cpf == null)
			cpf = (Long) session.getAttribute("cpf");
		else
			session.setAttribute("cpf", cpf);

		Paciente paciente = pacienteService.listarPorCPF(cpf);

		if (paciente != null) {
			model.addAttribute("paciente", paciente);
		}

		List<Sintoma> sintomas = Arrays.asList(Sintoma.values());
		model.addAttribute("sintomas", sintomas);

		return "paciente";
	}

	private boolean validarCpf(Long cpf) {
		// TODO: Implementar validação, o retorno está fixo!
		return true;
	}

	@PostMapping("/testar")
	public String testar(@Valid Paciente paciente, @RequestParam String[] sintomas, BindingResult erros,
			HttpSession session) {
		if (erros.hasErrors()) {
			return "paciente";
		}

		// calcular as chances de estar com o coronavírus
		double resultado = 0.0;
		for (int i = 0; i < sintomas.length; i++) {
			resultado += Double.parseDouble(sintomas[i]);
		}
		paciente.setResultado(resultado);

		try {
			if (validarCpf(paciente.getCpf())) {
				pacienteService.salvar(paciente);
				return "redirect:exibir";
			} else {
				// TODO erro se não validou CPF
				session.setAttribute("erro", "CPF inválido!");
				return "redirect:buscar";
			}
		} catch (Exception e) {
			session.setAttribute("erro", "Erro ao tentar salvar teste do paciente!" + e.getMessage());
			return "redirect:buscar";
		}

	}

	@RequestMapping("/exibir")
	public String exibir(HttpSession session, Model model) {
		Long cpf = (Long) session.getAttribute("cpf");
		if (cpf == null)
			return "/";
		else {
			Paciente paciente = pacienteService.listarPorCPF(cpf);
			model.addAttribute("paciente", paciente);
			return "resultado";
		}

	}

	@GetMapping("/rest/listarporresultado/{resultado}")
	@ResponseBody
	public List<Paciente> listarPorResultado(@PathVariable("resultado") Double resultado) {
		return pacienteService.listarPorResultado(resultado);
	}

	@GetMapping("/rest/listarporsexo/{sexo}")
	@ResponseBody
	public List<Paciente> listarPorSexo(@PathVariable("sexo") String sexo) {
		return pacienteService.listarPorSexo(sexo);
	}

}