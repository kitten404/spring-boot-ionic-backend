package com.quezia.cursomc.services;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class SmtpEmailService extends AbstractEmailService{

	@Autowired
	private MailSender mailSender;
	
	@Autowired
	private JavaMailSender javams;
	
	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MockEmailService.class);
	
	@Override
	public void SendEmail(SimpleMailMessage msg) {
		LOG.info("Enviando Email....");
		mailSender.send(msg);
		LOG.info("Email Enviado");
		
	}

	@Override
	public void sendHtmlEmail(MimeMessage msg) {
		LOG.info("Enviando Email...");
		javams.send(msg);
		LOG.info("Email Enviado");
		
	}

}
