package com.quezia.cursomc.services;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.licensemanager.model.AuthorizationException;
import com.quezia.cursomc.domain.Cidade;
import com.quezia.cursomc.domain.Cliente;
import com.quezia.cursomc.domain.Endereco;
import com.quezia.cursomc.domain.enums.Perfil;
import com.quezia.cursomc.domain.enums.TipoCliente;
import com.quezia.cursomc.dto.ClienteDTO;
import com.quezia.cursomc.dto.ClienteNewDTO;
import com.quezia.cursomc.repositories.ClienteRepository;
import com.quezia.cursomc.repositories.EnderecoRepository;
import com.quezia.cursomc.security.UserSS;

import javassist.tools.rmi.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private ImageService ims;
	@Value("${img.prefix.client.profile}")
	private String prefix;
	@Value("${img.profile.size}")
	private int size;
	@Autowired
	private BCryptPasswordEncoder ps;	
	@Autowired
	private ClienteRepository cr;
	@Autowired
	private EnderecoRepository er;
	@Autowired
	private S3Service s3;
	
	public Cliente buscar(Integer id) throws ObjectNotFoundException {
		
		UserSS user = UserService.authenticated();
		
		if(user ==null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationExcpetion("Acesso Negado");
		}
		
		Optional<Cliente> obj = cr.findById(id);
		
		return obj.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado! Id: " 
		+ id + ", Tipo: " + Cliente.class.getName()));
	}
	
	@Transactional
	public Cliente insert(Cliente obj){
		obj.setId(null);
		obj= cr.save(obj);
		er.saveAll(obj.getEnderecos());
		return obj;
	}
	
	public Cliente update(Cliente obj) throws ObjectNotFoundException{
		Cliente objCli = buscar(obj.getId());
		updateData(objCli,obj);
		return cr.save(objCli);
		
	}
	
	public void deletar(Integer id) {
		try {
			buscar(id);
		} catch (ObjectNotFoundException e) {
			// TODO Auto-generated objch block
			e.printStackTrace();
		}
		try {
			cr.deleteById(id);
		}catch(DataIntegrityViolationException ex){
			throw new DataIntegrityExcpetion("Não é possível exluir um objeto associado a outro");
		}
		
	}
	
	public List<Cliente> listarTudo(){
		return cr.findAll();
	}
	
	public Cliente findByEmail(String email) throws ObjectNotFoundException {
		
		UserSS user = UserService.authenticated();
		
		if(user == null)
			throw new AuthorizationException("Acesso Negado");
		
		Cliente obj = cr.findByEmail(email);
		
		if(obj == null) {
			throw new 
			ObjectNotFoundException("Objeto não encontrado ID: "+user.getId() +" Tipo: "+ Cliente.class.getName());
		}
		
		return obj;
	}
	
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return cr.findAll(pageRequest);
	}
	
	public Cliente fromDTO(ClienteDTO obj) {
		return new Cliente(obj.getId(), obj.getNome(),obj.getEmail(), null, null,null);
	}
	
	public Cliente fromDTO(ClienteNewDTO obj) {
		Cliente cli = new Cliente(null, obj.getNome(),obj.getEmail(),obj.getCpfOuCnpj(),TipoCliente.toEnum(obj.getTipo()),ps.encode(obj.getSenha()));
		Cidade cid = new Cidade(obj.getCidadeId(),null, null);
		Endereco end = new Endereco(null, obj.getLogradouro(),obj.getNumero(),obj.getComplemento(),obj.getBairro(),obj.getCep(),cid,cli);
		cli.getEnderecos().add(end);
		cli.getTelefones().add(obj.getTelefone1());
		if(obj.getTelefone2() != null) {
			cli.getTelefones().add(obj.getTelefone2());
		}
		if(obj.getTelefone3() != null) {
			cli.getTelefones().add(obj.getTelefone3());
		}
		
		return cli;
	}
	
	private void updateData(Cliente cliObj, Cliente obj) {
		cliObj.setNome(obj.getNome());
		cliObj.setEmail(obj.getEmail());
	}
	
	public URI uploadProfilePicture(MultipartFile multiPartFile) throws IOException {
		UserSS user = UserService.authenticated();
		
		if(user == null)
			throw new AuthorizationException("Acesso Negado");
		
		BufferedImage jpgImage = ims.getJpgImageFromFile(multiPartFile);
		jpgImage = ims.cropSquare(jpgImage);
		jpgImage = ims.resize(jpgImage, size);
		String fileName = prefix + user.getId() +".jpg";
		
		return s3.uploadFile(ims.getInputStream(jpgImage, "jpg"),fileName,"image");
	}
}
