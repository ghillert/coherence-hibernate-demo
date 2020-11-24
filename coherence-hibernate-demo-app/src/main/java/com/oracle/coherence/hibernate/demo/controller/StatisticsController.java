/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.hibernate.demo.controller;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Explicit controller for retrieving Statistics.
 *
 * @author Gunnar Hillert
 *
 */
@RestController
@RequestMapping(path="/api/statistics")
@Transactional()
public class StatisticsController {

	@Autowired
	private SessionFactory session;

	@GetMapping
	public Statistics getEvents(Pageable pageable) {
		final Statistics statistics = session.getStatistics();
		return statistics;
	}

}
