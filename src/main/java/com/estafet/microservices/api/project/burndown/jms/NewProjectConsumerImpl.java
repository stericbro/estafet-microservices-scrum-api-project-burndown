package com.estafet.microservices.api.project.burndown.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.estafet.microservices.api.project.burndown.model.ProjectBurndown;
import com.estafet.microservices.api.project.burndown.service.ProjectBurndownService;

import io.opentracing.Tracer;

@Component
public class NewProjectConsumerImpl implements NewProjectConsumer {

	@Autowired
	private Tracer tracer;
	
	@Autowired
	private ProjectBurndownService projectBurndownService;

	/* (non-Javadoc)
	 * @see com.estafet.microservices.api.project.burndown.jms.NewProjectConsumer#onMessage(java.lang.String)
	 */
	@Override
	@JmsListener(destination = "new.project.topic", containerFactory = "myFactory")
	public void onMessage(String message) {
		try {
			projectBurndownService.newProject(ProjectBurndown.fromJSON(message));
		} finally {
			tracer.activeSpan().close();
		}
	}

}
