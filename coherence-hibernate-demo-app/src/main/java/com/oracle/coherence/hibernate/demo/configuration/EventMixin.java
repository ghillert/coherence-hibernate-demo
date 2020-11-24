package com.oracle.coherence.hibernate.demo.configuration;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oracle.coherence.hibernate.demo.model.Person;

public abstract class EventMixin {
	@JsonIgnore public Set<Person> participants;
}
