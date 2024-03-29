package com.quezia.cursomc.resources;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.quezia.cursomc.domain.Categoria;
import com.quezia.cursomc.dto.CategoriaDTO;
import com.quezia.cursomc.services.CategoriaService;

import javassist.tools.rmi.ObjectNotFoundException;

@RestController
@RequestMapping(value="/categorias")
public class CategoriaResource {
	
	@Autowired
	private CategoriaService serv;
	
	@RequestMapping(value="/{id}",method=RequestMethod.GET)
	public ResponseEntity<?> findById(@PathVariable Integer id) throws ObjectNotFoundException {
		
		Categoria cat = serv.buscar(id);
		
		return ResponseEntity.ok().body(cat);
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')") 
	@RequestMapping(method=RequestMethod.POST)
	public ResponseEntity<Void> insert(@Valid @RequestBody CategoriaDTO cat){
		Categoria obj = serv.fromDTO(cat);
		obj = serv.insert(obj);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(cat.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')") 
	@RequestMapping(value="/{id}",method=RequestMethod.PUT)
	public ResponseEntity<Void> Update(@Valid @RequestBody CategoriaDTO cat, @PathVariable Integer id) throws ObjectNotFoundException{
		Categoria obj = serv.fromDTO(cat);
		obj.setId(id);
		obj = serv.update(obj);
		return ResponseEntity.noContent().build();
	}
	
	@PreAuthorize("hasAnyRole('ADMIN')") 
	@RequestMapping(value="/{id}",method=RequestMethod.DELETE)
	public ResponseEntity<Void> Delete(@PathVariable Integer id) {
		serv.deletar(id);
		return ResponseEntity.noContent().build();
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public ResponseEntity<List<CategoriaDTO>> FindAll() {
		List<Categoria> list = new ArrayList<>();
		list = serv.listarTudo();
		List<CategoriaDTO> cDTO = list.stream().map(obj -> new CategoriaDTO(obj)).collect(Collectors.toList());
		return ResponseEntity.ok().body(cDTO);
	}
	
	@RequestMapping(value="/page",method=RequestMethod.GET)
	public ResponseEntity<Page<CategoriaDTO>> FindPage(
			@RequestParam(value="page" ,defaultValue="0") Integer page, 
			@RequestParam(value="linesPerPage" ,defaultValue="24") Integer linesPerPage, 
			@RequestParam(value="orderBy", defaultValue ="nome")String orderBy, 
			@RequestParam(value="direction" ,defaultValue="ASC")String direction) {
		Page<Categoria> list = serv.findPage(page, linesPerPage, orderBy, direction);
		Page<CategoriaDTO> cDTO = list.map(obj -> new CategoriaDTO(obj));
		return ResponseEntity.ok().body(cDTO);
	}

}
