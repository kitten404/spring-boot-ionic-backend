package com.quezia.cursomc.dto;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import com.quezia.cursomc.domain.Categoria;

public class CategoriaDTO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	@NotEmpty(message ="Preenchimento Obrigatório")
	@Length(min =5, max=80 , message="O tamanho deve ser entre 5 e 80 caracters")
	private String nome;
	
	public CategoriaDTO(){
		
	}
	
	public CategoriaDTO(Categoria obj){
		this.id =obj.getId();
		this.nome = obj.getNome();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
}
