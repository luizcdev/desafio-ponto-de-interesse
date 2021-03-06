package com.digitalmaps.desafio.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.digitalmaps.desafio.core.constant.RegexPattern;
import com.digitalmaps.desafio.core.util.TimeUtil;
import com.digitalmaps.desafio.model.PointOfInterest;
import com.digitalmaps.desafio.service.PointOfInterestService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("pointofinterest")
@Validated
public class PointOfInterestController {
	
	@Autowired
	private PointOfInterestService pointOfInterestService;
	
	@ApiOperation(value = "Cria o ponto de interesse e retorna o objeto criado")
	@PostMapping(produces ="application/json", consumes="application/json")
	@Transactional(rollbackOn = Exception.class)
	public PointOfInterest save(@Valid @RequestBody PointOfInterest pointOfInterest) throws Exception {
		return pointOfInterestService.save(pointOfInterest);	
	}
	
	@ApiOperation(value="Retorna todos os pontos de interesse cadastrados")
	@GetMapping(produces ="application/json")
	@ResponseBody()
	public List<PointOfInterest> findAll() {
		return pointOfInterestService.findAll();
	}
	
	@ApiOperation(value="Retorna os pontos de interesse próximos, baseado na posição e no raio de distância "
			+ "e indica se o ponto está aberto ou fechado no horário informado")
	@GetMapping(value= "/proximity/{x}/{y}/{distance}/{time}", produces ="application/json")
	@ResponseBody
	public List<String> findByProximity(
			@ApiParam("Coordenada X da posição atual do usuário")
			@PathVariable("x") int x, 
			
			@ApiParam("Coordenada Y da posição atual do usuário") 
			@PathVariable("y") int y,
			
			@ApiParam("Distância máxima desejada")
			@PathVariable("distance") int distance, 
			
			@ApiParam("Horário atual em HH:mm")
			@PathVariable("time") 
			@Pattern(regexp = RegexPattern.TIME_HH_MM_24H, message = "Formato inesperado para o parâmetro time, utilize o padrão HH:mm")
			String time ) throws Exception {
	
		
		return pointOfInterestService.findByProximity(x, y, distance, TimeUtil.stringToTime(TimeUtil.PATTERN_HOUR_MINUTE, time));
	}
	
	//Trata os erro de validação, para retornar apenas as mensagens de críticas
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<List<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
		
		List<String> errorList = exception.getBindingResult().getAllErrors().stream()
				.map(ObjectError::getDefaultMessage)
				.collect(Collectors.toList());
		
		return new ResponseEntity<List<String>>(errorList, HttpStatus.BAD_REQUEST);
	}
	
	//Trata os erros de validação de PathVariable, para exibir apenas o parâmetro incorreto
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<String> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
		
		return new ResponseEntity<String>("Formato inesperado para o parâmetro: " + exception.getParameter().getParameterName(), HttpStatus.BAD_REQUEST);
	}
	
	//Trata os erros de excessão para retornar apenas a mensagem
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleValidationException(Exception exception) {
		
		return new ResponseEntity<String>(exception.getMessage(), HttpStatus.BAD_REQUEST);
	}
		
}
