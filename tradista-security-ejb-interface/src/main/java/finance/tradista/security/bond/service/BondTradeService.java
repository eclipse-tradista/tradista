package finance.tradista.security.bond.service;

import java.time.LocalDate;
import java.util.List;

import jakarta.ejb.Remote;

import finance.tradista.security.bond.model.BondTrade;

/*
 * Copyright 2016 Olivier Asuncion
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    */

@Remote
public interface BondTradeService {
	
	long saveBondTrade(BondTrade trade);

	List<BondTrade> getBondTradesBeforeTradeDateByBondAndBookIds(LocalDate date, long bondId, long bookId);

	BondTrade getBondTradeById(long id);

}
