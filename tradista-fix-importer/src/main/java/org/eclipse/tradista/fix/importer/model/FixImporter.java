package org.eclipse.tradista.fix.importer.model;

import java.io.InputStream;
import java.util.Optional;

import org.eclipse.tradista.core.common.exception.TradistaBusinessException;
import org.eclipse.tradista.core.common.model.TradistaObject;
import org.eclipse.tradista.core.importer.TradistaImporter;
import org.eclipse.tradista.fix.importer.processing.ImportApplication;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldNotFound;
import quickfix.FileStoreFactory;
import quickfix.Group;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.MessageCracker;
import quickfix.MessageCracker.Handler;
import quickfix.ScreenLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;
import quickfix.field.ExecType;
import quickfix.field.NoSides;
import quickfix.field.OrdStatus;
import quickfix.field.Symbol;
import quickfix.fix44.TradeCaptureReport;

public abstract class FixImporter extends TradistaImporter<TradeCaptureReport> {

	public static final String FIX = "Fix";

	private String configFileName;

	private MessageCracker messageCracker;

	@Override
	protected void start() {
		messageCracker = new MessageCracker(this);
		InputStream inputStream = this.getClass().getResourceAsStream(configFileName);
		SessionSettings settings;
		SocketAcceptor acceptor;
		try {
			settings = new SessionSettings(inputStream);

			ImportApplication application = new ImportApplication(this);
			FileStoreFactory storeFactory = new FileStoreFactory(settings);
			LogFactory logFactory = new ScreenLogFactory(settings);
			acceptor = new SocketAcceptor(application, storeFactory, settings, logFactory, new DefaultMessageFactory());
			acceptor.start();
		} catch (ConfigError ce) {
			// TODO Add error logs
			return;
		}
	}

	@Override
	public String getType() {
		return FIX;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	@Override
	protected void validateMessage(TradeCaptureReport fixMessage) throws TradistaBusinessException {
		try {
			messageCracker.crack(fixMessage, null);
		} catch (UnsupportedMessageType | FieldNotFound | IncorrectTagValue e) {
			throw new TradistaBusinessException(e);
		}
	}

	@Override
	protected Optional<? extends TradistaObject> processMessage(TradeCaptureReport externalMessage) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Handler
	public void onTradeCaptureReport(TradeCaptureReport tcReport, SessionID sessionID) {
		StringBuilder errMsg = new StringBuilder();
		String productType;

		checkExecTypeAndOrderStatusConsistency(extractExecType(tcReport, errMsg), extractOrderStatus(tcReport, errMsg),
				errMsg);

		checkSymbol(tcReport, errMsg);

		checkLastPx(tcReport, errMsg);

		checkLastQty(tcReport, errMsg);

		checkTradeDate(tcReport, errMsg);

		checkNoSides(tcReport, errMsg);

		checkMaturityDate(tcReport, errMsg);

		// Product type specific checks
		// 1. Get product type from message
		productType = getProductType(tcReport);
		
		if (productType != null) {
			
		}
	}

	protected String getProductType(TradeCaptureReport tcReport) {
		Symbol symbol = null;
		if (tcReport.isSetSymbol()) {
			try {
				symbol = tcReport.getSymbol();
			} catch (FieldNotFound fnfe) {
				// Not expected here.
			}
		}
		if (symbol != null && symbol.getValue().contains("GC")) {
			return "GCRepo";
		}
		return null;
	}

	private void checkMaturityDate(TradeCaptureReport tcReport, StringBuilder errMsg) {
		try {
			tcReport.getMaturityDate();
		} catch (FieldNotFound e) {
			errMsg.append(e);
		}
	}

	private void checkTradeDate(TradeCaptureReport tcReport, StringBuilder errMsg) {
		try {
			tcReport.getTradeDate();
		} catch (FieldNotFound e) {
			errMsg.append(e);
		}
	}

	private void checkLastQty(TradeCaptureReport tcReport, StringBuilder errMsg) {
		try {
			tcReport.getLastQty();
		} catch (FieldNotFound e) {
			errMsg.append(e);
		}
	}

	private void checkLastPx(TradeCaptureReport tcReport, StringBuilder errMsg) {
		try {
			tcReport.getLastPx();
		} catch (FieldNotFound e) {
			errMsg.append(e);
		}
	}

	private void checkSymbol(TradeCaptureReport tcReport, StringBuilder errMsg) {
		try {
			tcReport.getSymbol();
		} catch (FieldNotFound e) {
			errMsg.append(e);
		}
	}

	/**
	 * Managed execution types are new trades, update and cancellation. 'F' : Trade
	 * (normal execution), '0' : New (execution confirmation) ,'H' : Trade Cancel,
	 * 'G' : Trade Update
	 * 
	 * @param tcReport the fix message (TradeCaptureReport type)
	 * @param errMsg   the error message
	 * @return the execType if a valid value was found, null otherwise
	 * 
	 */
	private ExecType extractExecType(TradeCaptureReport tcReport, StringBuilder errMsg) {
		ExecType execType = null;
		try {
			execType = tcReport.getExecType();

			if (execType.getValue() != 'F' && execType.getValue() != '0' && execType.getValue() != 'H'
					&& execType.getValue() != 'G') {
				throw new IncorrectTagValue(execType.getField());
			}
		} catch (FieldNotFound | IncorrectTagValue e) {
			errMsg.append(e);
		}
		return execType;
	}

	/**
	 * Managed order status are filled (2) or canceled (4).
	 * 
	 * @param tcReport the fix message (TradeCaptureReport type)
	 * @param errMsg   the error message
	 * @return the ordStatus if a valid value was found, null otherwise
	 */
	private OrdStatus extractOrderStatus(TradeCaptureReport tcReport, StringBuilder errMsg) {

		OrdStatus ordStatus = null;
		try {
			ordStatus = tcReport.getOrdStatus();

			if (ordStatus.getValue() != '2' && ordStatus.getValue() != '4') {
				throw new IncorrectTagValue(ordStatus.getField());
			}
		} catch (FieldNotFound | IncorrectTagValue e) {
			errMsg.append(e);
		}
		return ordStatus;
	}

	/**
	 * No sides expected value is 2. The currency must be found in the Nosides
	 * group, as long as the party id.
	 * 
	 * @param tcReport the fix message (TradeCaptureReport type)
	 * @param errMsg   the error message
	 */
	private void checkNoSides(TradeCaptureReport tcReport, StringBuilder errMsg) {
		NoSides noSides = null;
		try {
			noSides = tcReport.getNoSides();
			if (noSides.getValue() != '2') {
				throw new IncorrectTagValue(noSides.getField());
			}
		} catch (FieldNotFound | IncorrectTagValue e) {
			errMsg.append(e);
		}

		if (noSides != null) {
			for (Group sideGroup : tcReport.getGroups(noSides.getField())) {
				try {
					((TradeCaptureReport.NoSides) sideGroup).getCurrency();
				} catch (FieldNotFound e) {
					errMsg.append(e);
				}
				try {
					for (Group partyGroup : tcReport
							.getGroups(((TradeCaptureReport.NoSides) sideGroup).getSide().getField())) {
						try {
							((TradeCaptureReport.NoSides.NoPartyIDs) partyGroup).getPartyID();
						} catch (FieldNotFound e) {
							errMsg.append(e);
						}
					}
				} catch (FieldNotFound e) {
					errMsg.append(e);
				}
			}
		}
	}

	/**
	 * Ensure execType and ordStatus have consistent values.
	 * 
	 * @param execType  the execution type
	 * @param ordStatus the order status
	 * @param errMsg    the error message
	 */
	private void checkExecTypeAndOrderStatusConsistency(ExecType execType, OrdStatus ordStatus, StringBuilder errMsg) {
		if (execType != null && ordStatus != null) {
			try {
				switch (execType.getValue()) {
				case 'F', '0', 'G':
					if (ordStatus.getValue() != '2') {
						throw new IncorrectTagValue(ordStatus.getField(), String.valueOf(ordStatus.getValue()), String
								.format("When the ExecType is %c, OrdStatus should be %c", execType.getValue(), '2'));
					}
					break;
				case 'H':
					if (ordStatus.getValue() != '4') {
						throw new IncorrectTagValue(ordStatus.getField(), String.valueOf(ordStatus.getValue()), String
								.format("When the ExecType is %c, OrdStatus should be %c", execType.getValue(), '4'));
					}
					break;
				}
			} catch (IncorrectTagValue ite) {
				errMsg.append(ite);
			}
		}
	}

}