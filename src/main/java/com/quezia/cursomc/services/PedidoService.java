package com.quezia.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quezia.cursomc.domain.Cliente;
import com.quezia.cursomc.domain.ItemPedido;
import com.quezia.cursomc.domain.PagamentoComBoleto;
import com.quezia.cursomc.domain.Pedido;
import com.quezia.cursomc.domain.Produto;
import com.quezia.cursomc.domain.enums.EstadoPagamento;
import com.quezia.cursomc.repositories.ClienteRepository;
import com.quezia.cursomc.repositories.ItemPedidoRepository;
import com.quezia.cursomc.repositories.PagamentoRepository;
import com.quezia.cursomc.repositories.PedidoRepository;
import com.quezia.cursomc.repositories.ProdutoRepository;
import com.quezia.cursomc.security.UserSS;

import javassist.tools.rmi.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository pr;
	
	@Autowired
	private BoletoService bs;
	
	@Autowired
	private PagamentoRepository pagto;
	
	@Autowired
	private ItemPedidoRepository ip;
	
	@Autowired
	private ClienteRepository cr;
	
	@Autowired
	private ProdutoRepository pdr;
	
	@Autowired
	private EmailService ems;
	
	public Pedido buscar(Integer id) throws ObjectNotFoundException {
		Optional<Pedido> obj = pr.findById(id);
		
		return obj.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado! Id: " 
		+ id + ", Tipo: " + Pedido.class.getName()));
	}
	
	@Transactional
	public Pedido insert(Pedido obj) throws ObjectNotFoundException {
		obj.setId(null);
		obj.setInstance(new Date());
		Optional<Cliente> optinalEntity = cr.findById(obj.getCliente().getId());
		obj.setCliente(optinalEntity.get());
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if(obj.getPagamento() instanceof PagamentoComBoleto ) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			bs.preencherPagamentoComBoleto(pagto, obj.getInstance());
		}
		obj = pr.save(obj);
		pagto.save(obj.getPagamento());
		
		for(ItemPedido ip: obj.getItens()) {
			ip.setDesconto(0.0);
			Optional<Produto> prod =pdr.findById(ip.getProduto().getId());
			ip.setProduto(prod.get());
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
		}
		ip.saveAll(obj.getItens());
		ems.sendOrderConfirmationHtmlEmail(obj);
		return obj;
	}
	
	public Page<Pedido> findPage(Integer page, Integer linesPerPage, String orderBy, String direction){
		UserSS user = UserService.authenticated();
		if(user == null)
			throw new AuthorizationExcpetion("Acesso Negado");

		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		Optional<Cliente> op =  cr.findById(user.getId());
		Cliente cli = op.get();
		return pr.findByCliente(cli, pageRequest);

	}
}
