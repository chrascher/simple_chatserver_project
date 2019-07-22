package at.cgsit.training.chatserver.common;


public enum ChatServerCommand {
	SENDHOSTNAME,
	CLOSECONNECTION,
	REFRESHTEXT,
	CREATENEWHOSTNAME,
	REQUESTACCEPT,
	ACCEPT,
	DENY,
	CONNECTIONESTABLISHED,
	INVALIDEREQUEST,
	INVALIDRESPONS,
	MESSAGERECIVED,
	ACOMPLISHMENT;
}