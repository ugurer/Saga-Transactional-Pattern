```mermaid
flowchart TD
    subgraph Client
        User[User/Client]
    end

    subgraph "Kafka & Event Bus"
        Kafka[Apache Kafka]
        Debezium[Debezium CDC]
    end

    subgraph "Monitoring & Observability"
        Prometheus[Prometheus]
        Grafana[Grafana Dashboard]
        Jaeger[Jaeger Tracing]
    end

    subgraph "Istio Service Mesh"
        Gateway[Istio Gateway]
        VirtualService[Virtual Service]
        DestRules[Destination Rules]
    end

    subgraph "Order Service"
        OrderAPI[Order API]
        OrderDB[(PostgreSQL)]
        OrderOutbox[(Outbox Table)]
    end

    subgraph "Inventory Service"
        InventoryAPI[Inventory API]
        InventoryDB[(PostgreSQL)]
        InventoryOutbox[(Outbox Table)]
    end

    subgraph "Payment Service"
        PaymentAPI[Payment API]
        PaymentDB[(PostgreSQL)]
        PaymentOutbox[(Outbox Table)]
    end

    subgraph "Shipping Service"
        ShippingAPI[Shipping API]
        ShippingDB[(PostgreSQL)]
        ShippingOutbox[(Outbox Table)]
    end

    %% Client interactions
    User -->|HTTP Request| Gateway
    Gateway -->|Route| VirtualService
    VirtualService -->|Routes to| OrderAPI
    VirtualService -->|Routes to| InventoryAPI
    VirtualService -->|Routes to| PaymentAPI
    VirtualService -->|Routes to| ShippingAPI

    %% Order flow
    OrderAPI -->|Stores| OrderDB
    OrderAPI -->|Writes to| OrderOutbox
    OrderOutbox -->|Captured by| Debezium
    Debezium -->|Publishes to| Kafka

    %% Inventory flow
    Kafka -->|Consumes OrderCreated| InventoryAPI
    InventoryAPI -->|Updates| InventoryDB
    InventoryAPI -->|Writes to| InventoryOutbox
    InventoryOutbox -->|Captured by| Debezium

    %% Payment flow
    Kafka -->|Consumes InventoryReserved| PaymentAPI
    PaymentAPI -->|Updates| PaymentDB
    PaymentAPI -->|Writes to| PaymentOutbox
    PaymentOutbox -->|Captured by| Debezium

    %% Shipping flow
    Kafka -->|Consumes PaymentCompleted| ShippingAPI
    ShippingAPI -->|Updates| ShippingDB
    ShippingAPI -->|Writes to| ShippingOutbox
    ShippingOutbox -->|Captured by| Debezium

    %% Monitoring flow
    OrderAPI -.->|Metrics| Prometheus
    InventoryAPI -.->|Metrics| Prometheus
    PaymentAPI -.->|Metrics| Prometheus
    ShippingAPI -.->|Metrics| Prometheus
    Prometheus -.->|Visualizes| Grafana
    
    %% Tracing
    OrderAPI -.->|Traces| Jaeger
    InventoryAPI -.->|Traces| Jaeger
    PaymentAPI -.->|Traces| Jaeger
    ShippingAPI -.->|Traces| Jaeger

    %% Compensation flows (dotted lines)
    Kafka -->|Error Events| InventoryAPI
    Kafka -->|Error Events| PaymentAPI
    Kafka -->|Error Events| ShippingAPI
    Kafka -->|Error Events| OrderAPI

    %% Style
    classDef kafka fill:#99c, stroke:#339, stroke-width:2px
    classDef service fill:#9c9, stroke:#393, stroke-width:2px
    classDef database fill:#fc9, stroke:#f96, stroke-width:2px
    classDef monitor fill:#c9c, stroke:#939, stroke-width:2px
    classDef mesh fill:#ccc, stroke:#999, stroke-width:2px
    
    class Kafka,Debezium kafka
    class OrderAPI,InventoryAPI,PaymentAPI,ShippingAPI service
    class OrderDB,InventoryDB,PaymentDB,ShippingDB,OrderOutbox,InventoryOutbox,PaymentOutbox,ShippingOutbox database
    class Prometheus,Grafana,Jaeger monitor
    class Gateway,VirtualService,DestRules mesh
```

## How to Convert This to PNG

To convert this Mermaid diagram to a PNG image:

1. **Option 1: Using Mermaid Live Editor**
   - Go to [Mermaid Live Editor](https://mermaid.live/)
   - Paste the diagram code
   - Click "Download PNG" to save the image

2. **Option 2: Using VS Code Extension**
   - Install the "Markdown Preview Mermaid Support" extension in VS Code
   - Open this file, and use the Markdown preview
   - Right-click on the diagram and select "Save image as..."

3. **Option 3: Using mermaid-cli**
   - Install mermaid-cli with npm: `npm install -g @mermaid-js/mermaid-cli`
   - Run: `mmdc -i architecture.md -o architecture.png`

Once you have the PNG file, replace the placeholder in docs/images/architecture.png with the actual image. 