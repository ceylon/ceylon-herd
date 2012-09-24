    alter table Comment 
        drop constraint FK9BDE863F856AF776;

    alter table Comment 
        drop constraint FK9BDE863FB2FABF16;

    alter table Dependency 
        drop constraint FK7540AF6B7BF02576;

    alter table Module 
        drop constraint FK89B0928CFA266C1E;

    alter table Module 
        drop constraint FK89B0928CB2FABF16;

    alter table ModuleComment 
        drop constraint FK6EA3E3353B2465E;

    alter table ModuleComment 
        drop constraint FK6EA3E33B2FABF16;

    alter table ModuleVersion 
        drop constraint FKE3396CAC53B2465E;

    alter table ModuleVersion_Author 
        drop constraint FK9BE1449E7BF02576;

    alter table ModuleVersion_Author 
        drop constraint FK9BE1449E42A7BA21;

    alter table Project 
        drop constraint FK50C8E2F9B2FABF16;

    alter table Upload 
        drop constraint FK9768FA21B2FABF16;

    alter table module_admin_user 
        drop constraint FK8CE50E6E555F243E;

    alter table module_admin_user 
        drop constraint FK8CE50E6E7080C8FC;

    drop table Author cascade;

    drop table Category cascade;

    drop table Comment cascade;

    drop table Dependency cascade;

    drop table Module cascade;

    drop table ModuleComment cascade;

    drop table ModuleVersion cascade;

    drop table ModuleVersion_Author cascade;

    drop table Project cascade;

    drop table Upload cascade;

    drop table module_admin_user cascade;

    drop table user_table cascade;

    drop sequence hibernate_sequence;

    create table Author (
        id int8 not null,
        name TEXT,
        primary key (id)
    );

    create table Category (
        id int8 not null,
        description TEXT,
        name varchar(255) not null unique,
        primary key (id)
    );

    create table Comment (
        id int8 not null,
        date timestamp,
        status varchar(255),
        text TEXT,
        owner_id int8,
        project_id int8,
        primary key (id)
    );

    create table Dependency (
        id int8 not null,
        export bool not null,
        name varchar(255),
        optional bool not null,
        version varchar(255),
        moduleVersion_id int8,
        primary key (id)
    );

    create table Module (
        id int8 not null,
        codeURL varchar(255),
        friendlyName varchar(255),
        homeURL varchar(255),
        issueTrackerURL varchar(255),
        name varchar(255) not null unique,
        category_id int8,
        owner_id int8 not null,
        primary key (id)
    );

    create table ModuleComment (
        id int8 not null,
        date timestamp,
        text TEXT,
        module_id int8,
        owner_id int8,
        primary key (id)
    );

    create table ModuleVersion (
        id int8 not null,
        ceylonMajor int4 not null,
        ceylonMinor int4 not null,
        doc TEXT,
        downloads int8 not null,
        isAPIPresent bool not null,
        isCarPresent bool not null,
        isJarPresent bool not null,
        isJsPresent bool not null,
        isRunnable bool not null,
        isSourcePresent bool not null,
        jsdownloads int8 not null,
        license TEXT,
        published timestamp not null,
        sourceDownloads int8 not null,
        version varchar(255) not null,
        module_id int8 not null,
        primary key (id),
        unique (module_id, version)
    );

    create table ModuleVersion_Author (
        ModuleVersion_id int8 not null,
        authors_id int8 not null
    );

    create table Project (
        id int8 not null,
        description TEXT,
        license varchar(255),
        moduleName varchar(255) not null,
        motivation TEXT,
        role varchar(255),
        status varchar(255),
        url varchar(255),
        owner_id int8 not null,
        primary key (id)
    );

    create table Upload (
        id int8 not null,
        created timestamp,
        owner_id int8,
        primary key (id)
    );

    create table module_admin_user (
        module int8 not null,
        admin int8 not null
    );

    create table user_table (
        id int8 not null,
        confirmationCode varchar(255) unique,
        email varchar(255) not null,
        firstName varchar(255),
        admin bool,
        lastName varchar(255),
        password varchar(255),
        salt varchar(255),
        status varchar(255) not null,
        userName varchar(255) unique,
        primary key (id)
    );

    alter table Comment 
        add constraint FK9BDE863F856AF776 
        foreign key (project_id) 
        references Project;

    alter table Comment 
        add constraint FK9BDE863FB2FABF16 
        foreign key (owner_id) 
        references user_table;

    alter table Dependency 
        add constraint FK7540AF6B7BF02576 
        foreign key (moduleVersion_id) 
        references ModuleVersion;

    alter table Module 
        add constraint FK89B0928CFA266C1E 
        foreign key (category_id) 
        references Category;

    alter table Module 
        add constraint FK89B0928CB2FABF16 
        foreign key (owner_id) 
        references user_table;

    alter table ModuleComment 
        add constraint FK6EA3E3353B2465E 
        foreign key (module_id) 
        references Module;

    alter table ModuleComment 
        add constraint FK6EA3E33B2FABF16 
        foreign key (owner_id) 
        references user_table;

    alter table ModuleVersion 
        add constraint FKE3396CAC53B2465E 
        foreign key (module_id) 
        references Module;

    alter table ModuleVersion_Author 
        add constraint FK9BE1449E7BF02576 
        foreign key (ModuleVersion_id) 
        references ModuleVersion;

    alter table ModuleVersion_Author 
        add constraint FK9BE1449E42A7BA21 
        foreign key (authors_id) 
        references Author;

    alter table Project 
        add constraint FK50C8E2F9B2FABF16 
        foreign key (owner_id) 
        references user_table;

    alter table Upload 
        add constraint FK9768FA21B2FABF16 
        foreign key (owner_id) 
        references user_table;

    alter table module_admin_user 
        add constraint FK8CE50E6E555F243E 
        foreign key (admin) 
        references user_table;

    alter table module_admin_user 
        add constraint FK8CE50E6E7080C8FC 
        foreign key (module) 
        references Module;

    create sequence hibernate_sequence;
