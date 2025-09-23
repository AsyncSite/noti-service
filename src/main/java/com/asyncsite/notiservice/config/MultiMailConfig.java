package com.asyncsite.notiservice.config;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "spring")
public class MultiMailConfig {

    private Map<String, MailConfigProperties> mailConfigs;

    public Map<String, MailConfigProperties> getMailConfigs() {
        return mailConfigs;
    }

    public void setMailConfigs(Map<String, MailConfigProperties> mailConfigs) {
        this.mailConfigs = mailConfigs;
    }

    @Bean
    @Primary
    public JavaMailSender defaultMailSender() {
        if (mailConfigs == null || !mailConfigs.containsKey("default")) {
            // Fallback to basic configuration if mail-configs not configured
            return createBasicMailSender();
        }
        return createMailSender(mailConfigs.get("default"));
    }

    @Bean("querydailyMailSender")
    public JavaMailSender querydailyMailSender() {
        if (mailConfigs == null || !mailConfigs.containsKey("querydaily")) {
            // Fallback to default if querydaily config not available
            return defaultMailSender();
        }
        return createMailSender(mailConfigs.get("querydaily"));
    }

    private JavaMailSender createMailSender(MailConfigProperties config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        if (config.getProperties() != null && config.getProperties().getMail() != null) {
            MailProperties.Smtp smtp = config.getProperties().getMail().getSmtp();
            if (smtp != null) {
                props.put("mail.smtp.auth", String.valueOf(smtp.isAuth()));
                if (smtp.getStarttls() != null) {
                    props.put("mail.smtp.starttls.enable", String.valueOf(smtp.getStarttls().isEnable()));
                }
                if (smtp.getConnectionTimeout() != null) {
                    props.put("mail.smtp.connectiontimeout", String.valueOf(smtp.getConnectionTimeout()));
                }
                if (smtp.getTimeout() != null) {
                    props.put("mail.smtp.timeout", String.valueOf(smtp.getTimeout()));
                }
                if (smtp.getWriteTimeout() != null) {
                    props.put("mail.smtp.writetimeout", String.valueOf(smtp.getWriteTimeout()));
                }
            }
        }

        return mailSender;
    }

    private JavaMailSender createBasicMailSender() {
        // Fallback implementation using basic Spring Mail properties
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        // This will be overridden by Spring's auto-configuration
        return mailSender;
    }

    public static class MailConfigProperties {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private String fromName;
        private MailPropertiesWrapper properties;

        // Getters and setters
        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public MailPropertiesWrapper getProperties() {
            return properties;
        }

        public void setProperties(MailPropertiesWrapper properties) {
            this.properties = properties;
        }
    }

    public static class MailPropertiesWrapper {
        private MailProperties.Mail mail;

        public MailProperties.Mail getMail() {
            return mail;
        }

        public void setMail(MailProperties.Mail mail) {
            this.mail = mail;
        }
    }

    public static class MailProperties {
        public static class Mail {
            private Smtp smtp;

            public Smtp getSmtp() {
                return smtp;
            }

            public void setSmtp(Smtp smtp) {
                this.smtp = smtp;
            }
        }

        public static class Smtp {
            private boolean auth;
            private Starttls starttls;
            private Integer connectionTimeout;
            private Integer timeout;
            private Integer writeTimeout;

            public boolean isAuth() {
                return auth;
            }

            public void setAuth(boolean auth) {
                this.auth = auth;
            }

            public Starttls getStarttls() {
                return starttls;
            }

            public void setStarttls(Starttls starttls) {
                this.starttls = starttls;
            }

            public Integer getConnectionTimeout() {
                return connectionTimeout;
            }

            public void setConnectionTimeout(Integer connectionTimeout) {
                this.connectionTimeout = connectionTimeout;
            }

            public Integer getTimeout() {
                return timeout;
            }

            public void setTimeout(Integer timeout) {
                this.timeout = timeout;
            }

            public Integer getWriteTimeout() {
                return writeTimeout;
            }

            public void setWriteTimeout(Integer writeTimeout) {
                this.writeTimeout = writeTimeout;
            }
        }

        public static class Starttls {
            private boolean enable;

            public boolean isEnable() {
                return enable;
            }

            public void setEnable(boolean enable) {
                this.enable = enable;
            }
        }
    }
}