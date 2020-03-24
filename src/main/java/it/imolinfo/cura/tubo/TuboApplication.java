package it.imolinfo.cura.tubo;

import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import it.imolainformatica.bootique.camel.CamelModule;

import javax.inject.Singleton;

public class TuboApplication implements BQModule {

    public static void main(String[] args) {
        Bootique.app(args).module(TuboApplication.class).autoLoadModules().exec().exit();

    }

    @Override
    public void configure(Binder binder) {
        BQCoreModule.extend(binder).addConfig("classpath:bootique-conf.yml");
        CamelModule.extend(binder).addRouteBuilder(TuboRouteBuilder.class);
    }

}
