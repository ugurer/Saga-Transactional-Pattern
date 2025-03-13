Kurumsal Düzeyde Dağıtık Transaction Yönetimi PoC
Giriş
Kurumsal ortamlarda mikroservis mimarisi üzerinde dağıtık işlemlerin tutarlı yönetimi kritik önemdedir. Geleneksel ana sistem (mainframe) uygulamalarındaki ACID işlemlerinin tutarlılığını, dağıtık bir mikroservis ortamında sağlamak güçtür. Tek bir işlem, birden çok bağımsız servisin veritabanını güncellemek zorunda kaldığında, tüm servislerin durumlarının uyum içinde tutulması gerekir. Bu PoC projesinin hedefi, SAGA tasarım deseni ile bu tutarlılığı sağlamaktır. Saga, uzun süreli ve birden fazla hizmeti içeren işlemleri tümüyle başarılı ya da tümüyle başarısız olacak şekilde yönetir​
INFOQ.COM
​
MICROSERVICES.IO
. Bu yaklaşım sayesinde, her bir mikroservis kendi lokal veritabanında işlem yaparken, tüm sistem genelinde “ya hep ya hiç” mantığında tutarlılık elde edilir. Ayrıca, servisler arası iletişim asenkron olaylar ile gerçekleşeceği için gevşek bağlılık (loose coupling) korunur ve tüm sistemin aynı anda müsait olması gerekmez​
INFOQ.COM
. Bu PoC’de, Kafka ve Debezium ile Outbox Pattern kullanılarak servisler arası mesajlaşma güvenli hale getirilecek, hata durumlarında compensating transaction (telafi edici işlemler) ile geri alma mekanizması kurulacaktır. Amaç, ana sistem seviyesinde işlem tutarlılığı elde eden, OpenShift üzerinde çalışabilen kurumsal düzeyde bir dağıtık transaction yönetimi çözümü ortaya koymaktır.
Mikroservis Mimarisi
PoC kapsamında dört adet Spring Boot tabanlı mikroservis tasarlanmıştır: Order Service, Inventory Service, Payment Service ve Shipping Service. Her servis, Database per Service ilkesine uygun olarak kendi PostgreSQL veritabanına sahiptir​
MICROSERVICES.IO
. Özetle:
Order Service: Müşteri siparişlerini yönetir. Yeni bir sipariş oluşturur, durumunu (ör. Pending, Completed, Cancelled) takip eder. Kendi veritabanında orders tablosunda sipariş kayıtlarını tutar.
Inventory Service: Ürün stoklarını yönetir. Siparişe konu ürünlerin stoktan rezerve edilmesi ve geri bırakılması (iade) işlemlerini yapar. Kendi veritabanında inventory kayıtlarını tutar.
Payment Service: Ödeme işlemlerini yönetir (kredi kartı veya bakiye kontrolü). Sipariş için ödeme tahsilatını yapar veya iptal durumunda iade işlemini gerçekleştirir. Kendi veritabanında ödeme kayıtlarını tutar.
Shipping Service: Kargo/teslimat işlemlerini yönetir. Ödemesi alınmış ve stoğu ayrılmış siparişin sevkiyatını planlar ve takip eder. Kendi veritabanında gönderi kayıtlarını tutar.
Her bir mikroservis bağımsız geliştirilip container imajı olarak paketlenecektir. Servisler arasında senkron doğrudan çağrı olmayacaktır. Bunun yerine her servis, belirli olayları (event) üretecek ve diğerlerinin olaylarını tüketecektir. Bu sayede servisler gevşek bağlanır, ölçeklenebilirlik artar ve bir servis geçici olarak devre dışı kalsa bile işlemler kuyruğa alınıp bekleyebilir.
Dağıtık Transaction Yönetimi (Saga ve Outbox Pattern)
Mikroservisler arası tutarlı işlem yönetimi için Saga Pattern uygulanmıştır. Saga, bir dağıtık işlemi oluşturan bir dizi lokal işlemin sıralı yürütülmesidir​
MICROSERVICES.IO
. Her mikroservis, kendisine ait bir lokal işlem (local transaction) yürütür ve kendi veritabanını ACID özelliklerinde günceller. Ardından, yaptığı değişiklik hakkında bir olay mesajı yayınlar. Bu olay, zincirdeki bir sonraki mikroservisin kendi işlemini tetikler​
MICROSERVICES.IO
. Eğer zincirdeki herhangi bir adımda bir lokal işlem iş kuralı nedeniyle başarısız olursa, Saga deseni gereği o ana kadar gerçekleştirilen işlemleri geri almak için telafi edici işlemler (compensating transactions) dizisi çalıştırılır​
MICROSERVICES.IO
. Otomatik rollback olmadığından, her servis kendi yaptığı değişikliği geri alacak bir telafi işlemi tanımlar. Bu sayede, dağıtık işlem sonunda sistem ya tamamen başarılı (tüm adımlar uygulandı) ya da tamamen geri alınmış (önceki adımların etkileri temizlendi) hale gelir. Bu PoC’de Saga deseni için Choreography yaklaşımı benimsenmiştir. Yani bir merkezi orkestratör servis yerine, her hizmet kendi olaylarını yayınlayıp diğerlerinin olaylarını dinleyerek reaksiyon verir​
MICROSERVICES.IO
. Örneğin, Order Service bir sipariş oluşturduğunda "OrderCreated" adlı bir olay yayınlar; Inventory Service bu olayı yakalayıp stok ayırma işlemini yapar ve başarılı olursa "InventoryReserved" olayını yayınlar. Payment Service, "InventoryReserved" olayını yakalayıp ödemeyi tahsil eder ve "PaymentCompleted" olayı yayınlar. Son olarak Shipping Service, "PaymentCompleted" ile tetiklenerek sevkiyat işlemini yapar. Bu koreografi sırasında herhangi bir adım başarısız olursa ilgili servis bir hata olayı yayınlar ve diğer servisler kendi telafi işlemlerini yapar. Örneğin Payment Service ödeme onaylayamazsa "PaymentFailed" olayı yayınlayacak, bunu duyan Inventory Service ayırdığı stoğu serbest bırakacak, Order Service ise siparişi "Cancelled" durumuna getirecektir. Bu tamamen dağıtık ve asenkron tasarım, merkezi bir kontrol noktası olmadığından tekil hata noktalarını ortadan kaldırır ve servisler arası zayıf bağımlılığı korur​
MICROSERVICES.IO
. Transactional Outbox Pattern, Saga’nın güvenli şekilde uygulanabilmesi için kullanılmaktadır. Outbox deseninde, bir servis kendi veritabanında iş verisini güncellerken aynı işlem (transaction) içinde bir Outbox tablosuna da olay kaydı yazar​
INFOQ.COM
. Bu sayede servis, ayrı bir mesaj kuyruğuna doğrudan gönderim yapmadan, yerel veritabanına tek bir atomik işlem yapmış olur. Ardından devreye giren Debezium gibi bir Change Data Capture (CDC) aracı, veritabanı değişikliklerini dinleyerek Outbox tablosuna eklenen olayı tespit eder ve Apache Kafka aracılığıyla ilgili olayı mesaj olarak yayınlar​
INFOQ.COM
​
DEBEZIUM.IO
. Bu yaklaşım, klasik çift-yazma (dual write) problemine çözüm getirir: Servis, veritabanını güncelledikten sonra bir de mesaj kuyruğuna yazmak zorunda kalmaz, böylece iki kaynak arasında dağıtık işlem yapmaya gerek kalmaz​
INFOQ.COM
. Mesajın gönderimi veritabanı işleminin bir uzantısı olarak garanti altına alınır. Debezium, PostgreSQL veritabanlarının WAL (Write-Ahead Log) kayıtlarını dinleyerek Outbox tablosundaki eklemeleri tespit eder ve olayı Kafka’ya iletir​
DEBEZIUM.IO
. Her mikroservisin veritabanında bir Outbox tablosu olduğu için, her servis kendi ürettiği olayları Kafka’ya bu yolla yayınlayacaktır. Kafka tarafında olaylar, örneğin konu bazında (topic) servis veya işlem tipine göre ayrıştırılabilir (örn. Order servisinin olayları order-events konusuna, Inventory’nin olayları inventory-events konusuna gidebilir). Debezium’un Outbox Event Router gibi özellikleri, bu olayların uygun Kafka konularına yönlendirilmesine yardımcı olur​
DEBEZIUM.IO
​
DEBEZIUM.IO
. 


Şekil 1: Outbox Pattern ile dağıtık olay yayınlama mimarisi – Her servis kendi veritabanına (PostgreSQL) yazdığı Outbox tablosu üzerinden olayları üretiyor. Debezium (Kafka Connect içindeki CDC konektörü) veritabanı günlüklerini izleyerek Outbox’taki kayıtları Kafka konularına iletiyor. Diğer servisler bu konuları tüketerek ilgili işlemlerini tetikliyor. Alt kısımda örnek bir Outbox tablosu yapısı ve içeriği görülüyor​
DEBEZIUM.IO
. Yukarıdaki mimaride, örneğin Order Service kendi Order DB veritabanına “OrderCreated” olayı ekler eklemez, Debezium Postgres konektörü bu olayı yakalayıp Kafka üzerindeki Order Events konusuna bir mesaj olarak atar. Inventory Service, Kafka üzerinden Order Events mesajlarını dinleyerek “OrderCreated” mesajını alır ve kendi işlemine başlar. Bu esnada Order Service, veritabanı işlemini başarıyla tamamladığı için “kendi yazdığını okuma” (read your own writes) tutarlılığına da sahiptir; yani outbox’a yazdığı olayı gerekirse kendi veritabanından hemen görebilir​
DEBEZIUM.IO
. Böylece sistem genelinde etkin bir sonuç tutarlılığı (eventually consistent) sağlanırken, her servis kendi verisinde anlık tutarlılığı korur. Telafi edici işlemler (Compensation): Saga deseninin bir parçası olarak, her adımda başarısızlık durumunda geriye dönük toparlama yapılmalıdır​
MICROSERVICES.IO
. Bu PoC’de her mikroservis, gerçekleştirdiği işlemin aksini yapacak bir telafi mantığı içerir. Örneğin: Inventory Service stok ayırdıktan sonra bir sonraki adımda (ödeme) sorun çıkarsa, “PaymentFailed” olayını dinleyerek ayırdığı stoğu iade eder. Payment Service, ödeme başarılı olduktan sonraki adımda (kargo) sorun çıkarsa “ShippingFailed” olayını dinleyerek yapılan ödemeyi geri alır (iade işlemi). Order Service ise herhangi bir hata olayı aldığında sipariş durumunu Cancelled yaparak süreci sonlandırır. Bu telafi işlemleri de kendi veritabanları üzerinde ACID özelliklerinde gerçekleştirilir, ve gerekiyorsa diğer servisleri bilgilendirmek için yeni olaylar yayınlanabilir. Böylece, sistem bir hata durumunda başlangıçtaki hale en yakın konsisten duruma geri döner.
Kubernetes/OpenShift Dağıtım Yapılandırmaları
Projedeki tüm bileşenler, konteyner olarak paketlenip Kubernetes/OpenShift ortamında çalışacak şekilde yapılandırılmıştır. Dağıtım için gerekli tüm YAML manifest dosyaları hazırlanmıştır. Bu dosyalar, mikroservislerin ve destekleyici altyapı bileşenlerinin tanımlarını içerir:
Mikroservis Deployment’ları: Her bir Spring Boot tabanlı servis için ayrı bir Deployment tanımı bulunmaktadır. Bu deployment, ilgili servis container imajını (örn. order-service:1.0) istediğimiz replikada çalıştırır. OpenShift’in gereksinimlerine uygun olarak ortam değişkenleri (örn. veritabanı bağlantı URL’i, Kafka broker adresi, vb.) ConfigMap/Secret şeklinde tanımlanmış ve Deployment’lara mount edilmiştir. Her servisin iletişim kurabilmesi için bir Kubernetes Service objesi tanımlıdır (istendiğinde OpenShift Route ile dış erişim de sağlanabilir).
PostgreSQL Veritabanları: Her mikroservis için ayrı bir PostgreSQL instance’ı konteyner olarak deploy edilmektedir (Deployment veya StatefulSet ile). Her Postgres veritabanı uygun kalıcılık (PersistentVolumeClaim) ile yapılandırılmış, ilgili servis dışında erişime kapalı tutulmuştur. Order, Inventory, Payment, Shipping servislerine ait veritabanı şemaları ve outbox tabloları initialization script’lerle oluşturulmaktadır.
Apache Kafka ve Debezium: Dağıtık olay altyapısı için Kafka kümesi ve Debezium CDC bileşeni kuruludur. OpenShift ortamında Kafka kurulumu için Strimzi Kafka Operator (AMQ Streams) kullanılabilir. YAML dosyaları, tek düğümlü bir Kafka ve Zookeeper (PoC için minimal) veya isteğe göre çok düğümlü bir cluster kurulumunu tanımlar. Kafka üzerinde kullanılacak konular (topics) gerekli replikasyon ve bölüm (partition) sayılarıyla tanımlanmıştır. Debezium, Kafka Connect platformu üzerinde çalışan bir eklenti olarak devreye alınır. Her bir servis veritabanı için bir Debezium PostgreSQL connector konfigürasyonu vardır. Bu konfigürasyonlar, ilgili veritabanının Outbox tablosunu dinleyerek değişiklikleri yakalayacak şekilde ayarlanmıştır. Örneğin, Order Service’in outbox’ı için bir connector, Payment için ayrı bir connector tanımı YAML içinde bulunur. Debezium connector’ları Outbox Pattern’e özgü Event Router SMT ayarıyla konfigure edilmiştir; bu sayede Outbox tablosundaki kaydın tipine göre otomatik olarak ilgili Kafka konusuna yönlendirme yapılır​
DEBEZIUM.IO
. Tüm Kafka ve Debezium bileşenleri de OpenShift üzerinde container olarak çalıştığından, PoC ortamı tamamen taşınabilir ve bulutta ölçeklenebilir olacaktır.
YAML Örüntüsü: PoC dokümantasyonunda, her bir bileşen için hazırlanmış örnek YAML dosyalarına yer verilmiştir. Örneğin order-service-deployment.yaml, inventory-service-deployment.yaml gibi servis tanımları; kafka-cluster.yaml Kafka operator tanımı; Debezium connector’ları için kafka-connect-outbox-connectors.yaml vb. Tüm bu manifestler, OpenShift ortamına oc apply komutu ile uygulanarak tek komutla tüm mimariyi ayağa kaldırmaya imkan tanır. Bu, kurumsal düzeyde bir Infrastructure as Code yaklaşımını da yansıtmaktadır.
Ayrıca, OpenShift ortamına özgü gereksinimler (güvenlik konteyner yetkileri, proje/namespace yapısı, image pull secret vs.) göz önüne alınarak manifestler düzenlenmiştir. Örneğin, her mikroservis DeploymentConfig yerine standart Deployment olarak tanımlanmış ve rolling update stratejileri belirlenmiştir. Bu sayede PoC, OpenShift 4.x üzerinde sorunsuz bir şekilde çalışabilir hale getirilmiştir.
Servis Mesh ve Gözlemlenebilirlik
Mikroservislerin dağıtık ortamdaki iletişimini yönetmek ve sistem genelinde gözlemlenebilirliği artırmak için Istio tabanlı bir Service Mesh entegrasyonu gerçekleştirilmiştir. OpenShift ortamında OpenShift Service Mesh operator’ü kullanılarak Istio kolayca devreye alınmıştır. Tüm mikroservis pod’larına Istio’nun Envoy sidecar proxy’leri otomatik enjekte edilmiştir. Service Mesh bize aşağıdaki faydaları sağlamaktadır:
Servis Keşfi ve Yük Dengeleme: Istio, Kubernetes servis kayıtlarını kullanarak mikroservislerin birbirini keşfetmesini ve isteklerin Envoy proxy üzerinden doğru hedefe yönlendirilmesini sağlar. Kod seviyesinde özel bir discovery mekanizmasına ihtiyaç kalmaz. Envoy proxy’ler gelen trafiği akıllıca ilgili servisin sağlıklı pod’larına dağıtır.
Arıza Toleransı ve Ağ Politikaları: Istio, mikroservisler arası trafiğe politika uygulama imkanı verir. Örneğin, belirli bir servisin yanıt vermemesi durumunda timeout ve retry (yeniden deneme) politikaları tanımlanmıştır. Bu sayede geçici aksaklıklarda Saga akışı hemen başarısız olmaz, belirli tekrar denemeler yapılabilir. Ayrıca Istio, devre kesici (circuit breaker) desenini mesh seviyesinde uygularak bir serviste sorun olduğunda aşırı yüklenmesini engeller​
INFOQ.COM
. Örneğin Payment Service devre dışı kalırsa, Envoy proxy kısa süreliğine çağrıları kesip daha sonra kademeli olarak kabul edebilir. Istio ile gelen fault injection özelliği, test ortamında yapay hatalar ekleyerek sistemin dayanıklılığını sınamamıza da olanak tanır​
INFOQ.COM
. Tüm bunlar uygulama koduna dokunmadan, konfigürasyonla sağlanır (“uygulama kodunda değişiklik yapmadan hata toleransı” sağlanır​
INFOQ.COM
).
Güvenlik: Service Mesh, servisler arası trafiği şifreleyerek (mTLS ile) güvenliği artırır. Bu PoC’de servisler arası tüm HTTP çağrılar (burada asenkron ileti de proxy üzerinden akar) Istio sayesinde otomatik olarak şifrelenmiştir. Ayrıca istek bazında yetkilendirme kuralları (Policy ve AuthorizationPolicy) ile sadece belirli servislerin belirli konulara erişmesi sağlanabilir.
Monitoring (Prometheus & Grafana): Istio servis mesh, tüm servislerin istek metriklerini otomatik olarak toplar (istek sayısı, gecikme süresi, hata oranları vb.). Prometheus, mesh içindeki Envoy proxy’lerin ve uygulamaların metriklerini scrape etmek üzere konfigüre edilmiştir​
ISTIO.IO
. Spring Boot servisleri de Micrometer/Prometheus metrik endpoint’leri ile kendi iş metriklerini (ör. işlem sayısı, bellek kullanımı, vb.) Prometheus’a sunar. OpenShift’in dahili Prometheus operatörü veya ayrı bir Prometheus deployment’ı kullanılarak bu metrikler toplanmaktadır. Grafana, bu metriklerin görselleştirilmesi için kuruludur. Istio için hazır gelen paneller (service mesh dashboard’ları) Grafana’ya import edilmiştir​
ISTIO.IO
. Böylece ekip, gerçek zamanlı olarak her bir mikroservisin performansını, istek yoğunluğunu ve hata durumlarını Grafana arayüzünden izleyebilir. Örneğin Grafana üzerinden “Order -> Inventory -> Payment -> Shipping” akışının gecikmeleri veya hata oranları anlaşılabilir. Bu saydamlık, olası darboğaz veya sorun noktalarını tespit etmeyi kolaylaştırır.
Distributed Tracing (Jaeger): Dağıtık bir transaction akışında, bir isteğin uçtan uca takibi için Jaeger tabanlı izleme entegrasyonu gerçekleştirilmiştir. OpenShift Service Mesh, trafiğin izlenebilmesi için Envoy proxy’lerde tracing’i aktif edecek şekilde yapılandırıldı. Ayrıca Spring Boot servislerinde OpenTracing/Jaeger entegrasyonu (örn. Spring Cloud Sleuth ile) etkinleştirildi. Bu sayede her bir sipariş işlemi için oluşan tüm alt işlem adımları (span’ler), ortak bir trace-id ile zincirlendi. Bir müşterinin sipariş isteği geldiğinde Order Service’de bir trace başlar, Inventory ve Payment servislerinde bu trace-id ile devam eder ve Shipping’de sonlanır. Jaeger UI üzerinden bu dağıtık işlem zinciri grafik olarak izlenebilir​
INFOQ.COM
. Örneğin Jaeger, bir sipariş Saga akışının hangi servislerde ne kadar süre harcadığını, sırayla hangi olayların tetiklendiğini net şekilde gösterir. Bu PoC’de Jaeger, All-in-One modunda OpenShift üzerinde çalıştırılmış ve Grafana Tempo gibi alternatiflerle de entegrasyon değerlendirilebilir. Tracing sayesinde, sistemde oluşan bir hatanın hangi adımda gerçekleştiği veya performans sorunlarının hangi servisten kaynaklandığı kolaylıkla tespit edilebilir.
CI/CD Entegrasyonu
Kurumsal ortamlarda sürekli entegrasyon ve dağıtım (CI/CD) süreçlerinin otomasyonu büyük önem taşır. Bu PoC için OpenShift Pipelines (Tekton) ve OpenShift GitOps (Argo CD) araçlarıyla CI/CD boru hattı oluşturulmuştur.
Sürekli Entegrasyon (CI) – Tekton Pipelines: Kaynak kodda yapılan her değişiklik sonrası otomatik olarak derleme, test ve imaj oluşturma adımlarını çalıştıran bir Tekton pipeline tanımlanmıştır. Tekton, Kubernetes tabanlı bir CI/CD framework’üdür ve OpenShift Pipelines buna dayanır​
REDHAT.COM
. Pipeline adımları (Task’lar) şunları yapmaktadır:
Kod Alımı: İlgili mikroservisin Git deposunu klonlar (örn. dev branch).
Derleme ve Test: Maven ile projeyi derler ve birim testlerini çalıştırır.
Container İmajı İnşası: Başarılı testlerden sonra, örneğin Buildah veya Kaniko kullanarak container imajını üretir (Dockerfile tanımına göre). İmaj versiyonu, Git commit ID veya sürüm numarasına göre etiketlenir.
Güvenlik Tarama: İmaj oluştuktan sonra Trivy gibi bir araçla güvenlik açığı taraması yapacak bir adım eklenmiştir (opsiyonel, kurumsal güvenlik gereksinimleri için)​
REDHAT.COM
.
Registry’e Push: İmaj, OpenShift’in entegre image registry’sine veya harici bir kurumsal registry’ye push edilir.
Deployment (CI Aşaması): Tekton, isteğe bağlı olarak, yeni imajı test ortamına deploy edebilir. Bunu doğrudan Kubernetes’e apply komutu ile yapabileceği gibi, GitOps yaklaşımıyla imaj bilgisini bir manifest depoya yazarak da yapabilir.
Tekton Pipeline’ı, tetikleyiciler ile entegre edilmiştir. Örneğin bir Git commit veya Merge olduğunda, otomatik olarak pipeline’ı başlatan bir Tekton Trigger konfigürasyonu mevcuttur. CI aşamaları sonucunda artefakt olarak çalışan ve testleri geçmiş container imajları elde edilir.
Sürekli Dağıtım (CD) – Argo CD (GitOps): Dağıtım aşamasında GitOps yaklaşımı tercih edilmiştir. Argo CD, Kubernetes için deklaratif sürekli dağıtım aracıdır ve Git deposundaki manifestlerin cluster ile senkronize olmasını otomatik hale getirir​
CODEFRESH.IO
. Bu PoC’de, tüm Kubernetes/OpenShift YAML manifestleri (servis deployment’ları, konfigürasyonlar vs.) bir Git repository’sinde (ör. ops-config deposu) versiyonlanmıştır. Argo CD, bu depo ile OpenShift cluster’ını eşitlemek üzere konfigüre edilmiştir. Pipeline, yeni bir imaj oluşturup registry’ye attıktan sonra, ilgili Kubernetes Deployment YAML dosyasında imaj tag’ini güncelleyip bu değişikliği GitOps deposuna işler. Argo CD bu değişikliği algılar ve OpenShift üzerindeki uygulamayı yeni imaja günceller​
CODEFRESH.IO
. Bu sayede dağıtım işlemi tamamen otomatik ve versiyon kontrollü gerçekleşir. Argo CD’nin web arayüzü üzerinden, tüm mikroservislerin hangi versiyonda olduğu, manifestlerin durumu izlenebilir​
CODEFRESH.IO
. Herhangi bir sapma (drift) durumunda Argo CD bunu tespit edip tekrar deklaratif tanıma göre düzeltme yapar. Bu GitOps yaklaşımı, farklı ortamlar (dev/qa/prod) arasında tutarlı ve denetimli dağıtım sağladığı için tercih edilmiştir​
CODEFRESH.IO
.
Not: Tekton ve Argo CD entegrasyonu sayesinde CI ve CD süreçleri birbirinden ayrılarak uzmanlaşmıştır​
CODEFRESH.IO
​
CODEFRESH.IO
. Tekton kod derleme/test ve paketleme (CI) işini yaparken, Argo CD dağıtım ve ortam yönetimi (CD) işini yapar​
CODEFRESH.IO
. Bu ayrım, hataların daha hızlı bulunmasını ve süreçlerin ölçeklenebilmesini sağlar. Ayrıca, istenirse Tekton pipeline’ı başarılı olduktan sonra Argo CD’yi tetiklemek için bir webhook veya Argo CD CLI kullanılabilir. Alternatif olarak, tamamen Tekton ile CI/CD uygulanabilirdi; ancak GitOps yaklaşımı kurumsal tutarlılık ve denetim için tercih edildi.
Bu CI/CD yapısı sayesinde, PoC kapsamındaki uygulamanın yeni bir versiyonunu çıkarmak sadece kod değişikliği yapıp Git’e göndermek kadar basittir. Gerisini pipeline ve Argo CD otomatik halleder, böylece insan hatası en aza iner ve hızlı yineleme (rapid iteration) imkanı sağlanır.
Örnek Transaction Akışı ve Hata Senaryoları
Aşağıda, sipariş oluşturma ile başlayan bir Saga akışının başarılı ve hatalı senaryoları adım adım açıklanmıştır. Bu senaryolar, PoC’nin test kılavuzu olarak da kullanılacaktır: ✔️ Başarılı Sipariş İşleme Senaryosu:
Sipariş Oluşturma: Müşteri, Order Service’e bir POST /orders isteği atarak yeni bir sipariş oluşturur. Order Service, isteği alır almaz kendi veritabanında bir kayıt oluşturur (örn. Order ID=100, durum=“PENDING”). Aynı işlem içerisinde Order Service, kendi Outbox tablosuna “OrderCreated” adlı bir olay ekler (içeriğinde sipariş kimliği ve detaylar bulunur)​
MICROSERVICES.IO
. Transaction commit olur olmaz, Debezium bu yeni outbox kaydını yakalar.
Olay Yayını (OrderCreated): Debezium, Order Service veritabanındaki OrderCreated outbox kaydını Kafka üzerindeki ilgili konuya iletir (örn. order-service-events topic). Artık sipariş oluşturulduğuna dair bir etkinlik sistem geneline duyurulmuştur.
Stok Rezervasyonu (Inventory Service): Inventory Service, Kafka üzerindeki OrderCreated mesajını tüketir. Yeni bir sipariş geldiğini öğrenen Inventory Service, kendi veritabanında ilgili ürünlerin stok kayıtlarını günceller (stok düşer veya rezerv konulur). Örneğin sipariş edilen ürünlerden elde yeterli miktar varsa, bu ürünler rezerve edilir. Bu lokal işlem başarılı olunca, Inventory Service kendi Outbox tablosuna “InventoryReserved” olayını yazar (içeriğinde ilgili Order ID ve rezerve edilen kalemler bulunur).
Olay Yayını (InventoryReserved): Debezium’un Inventory Service üzerindeki connector’ı, veritabanına eklenen InventoryReserved kaydını tespit edip Kafka’ya inventory konusuna gönderir. Bu olay, stokların başarıyla ayrıldığını bildirir.
Ödeme Tahsilatı (Payment Service): Payment Service, InventoryReserved mesajını dinler ve alır. Bu, siparişin ödemesinin alınabileceği anlamına gelir (stok mevcut). Payment Service, örneğin ilgili müşterinin kredi kartından veya hesabından ödeme çekme işlemini başlatır. Kendi veritabanında bir ödeme kaydı oluşturur (Order ID=100, ödeme tutarı, durum=“PAID”). Ödeme işlemi başarılı olursa, Payment Service outbox’ına “PaymentCompleted” olayını ekler. (Ödeme onayı alamazsa PaymentFailed yazılacaktı, o senaryo aşağıda ele alınmıştır.)
Olay Yayını (PaymentCompleted): Debezium, Payment Service veritabanındaki PaymentCompleted kaydını Kafka’ya iletir. Bu olay, sipariş ödemesinin başarıyla alındığını tüm ilgili servislere duyurur.
Kargo İşlemi (Shipping Service): Shipping Service, Kafka’dan PaymentCompleted olayını alır almaz sevkiyat sürecini başlatır. Bu adımda Shipping Service, kendi veritabanında yeni bir kargo gönderi kaydı oluşturur (Order ID=100, durum=“SHIPPING” gibi). Örneğin uygun bir kargo takip numarası atayabilir. İşlem tamamlandığında Shipping Service outbox tablosuna “OrderShipped” (veya ShippingCompleted) olayını yazar.
Olay Yayını (OrderShipped): Debezium, OrderShipped kaydını Kafka’ya iletir. Bu, Saga akışının son adımıdır; siparişin başarıyla kargoya verildiğini temsil eder.
Sipariş Tamamlama (Order Service): Order Service, OrderShipped olayını dinleyerek kendi siparişinin durumunu günceller. Order ID=100 için durum artık “COMPLETED” (tamamlandı) yapılır. Böylece başlangıçta PENDING olarak açılan siparişin tüm işlemleri başarıyla bitmiş, Saga normal akışı tamamlanmıştır. Order Service gerekirse müşteriye siparişin tamamlandığına dair bildirim gönderebilir ya da event yoluyla başka sistemlere haber verebilir.
Bu başarılı senaryoda görüldüğü üzere, her servis kendi görevini yapıp bir sonraki adıma sinyal veren bir event yayınlayarak zincir reaksiyonu oluşturdu. Tüm adımlar geçildiği için sipariş sorunsuz tamamlandı. Şimdi, olası bir aksaklık durumunda sistemin nasıl tepki verdiğine bakalım: ❌ Hata Durumu Senaryosu (Örneğin Kargo Aşamasında Hata):
Aynı işlemi bu kez son adımdaki bir hata ile düşünelim. Diyelim ki Shipping Service, lojistik sistemde bir arıza nedeniyle gönderi oluştururken hata verdi. Bu durumda süreç şu şekilde ilerler:
OrderCreated, InventoryReserved, PaymentCompleted adımları başarıyla gerçekleşir (yukarıda 1-6 adımları aynen gerçekleşti; sipariş verildi, stok ayrıldı, ödeme alındı). Sistem siparişi hazırlamıştır, kargoya verme aşamasındadır.
Kargo Hatası: Shipping Service, PaymentCompleted olayını aldıktan sonra gönderi kaydı oluşturmaya çalışırken bir hata oluşur (örneğin kargo hizmeti API’ı cevap vermedi veya iş kuralı hatası). Bu lokal işlem başarısız olunca, Shipping Service işlem başarısızlık durumunu algılar. Bu noktada Shipping Service, kendi veritabanındaki değişiklikleri geri alır (eğer kısmen bir şey eklediyse rollback yapar) ve outbox tablosuna bir hata olayı yazar: “ShippingFailed”. Bu olay, ilgili siparişin kargo adımında sorun çıktığını belirtir.
Olay Yayını (ShippingFailed): Debezium, Shipping Service outbox’ına eklenen ShippingFailed kaydını Kafka’ya iletir. Artık diğer servisler de bu siparişin tamamlanamayacağını, telafi işlemlerine başlanması gerektiğini anlar.
Ödeme Telafisi (Payment Service): Payment Service, ShippingFailed mesajını dinler. Siparişin teslim edilemediğini öğrenince, daha önce yaptığı işlemi geri almalıdır. Payment Service bu durumda müşteriden alınmış ödemeyi iade eder (veya ödeme kaydını “Refunded” durumuna çeker). Ödeme iadesi de kendi veritabanında bir lokal işlem olarak gerçekleştirilir. Gerekirse Payment Service outbox’a “PaymentRefunded” gibi bir olay yazabilir (bu, muhasebe/finansal sistemlerin haberdar olması için yapılabilir).
Stok Telafisi (Inventory Service): Inventory Service de ShippingFailed olayını alır. Bu da demektir ki, ayrılmış olan stokları tekrar kullanılabilir hale getirmek gerekir. Inventory Service, daha önce InventoryReserved ile rezerve ettiği stok kayıtlarını güncelleyerek rezervasyonu kaldırır (stok miktarını iade eder). Bu da lokal bir veritabanı güncellemesidir. Gerekirse Inventory Service de outbox’ına “InventoryReleased” gibi bir event yazabilir (stok iadesi tamamlandı olayı).
Sipariş İptali (Order Service): Hem Payment hem Inventory telafi işlemlerini yaptıktan sonra (hatta bunlarla paralel de olabilir), Sipariş sürecinin iptali onaylanmalıdır. Order Service, ya doğrudan ShippingFailed olayını dinleyerek, ya da diğer servislerin telafi olaylarını (PaymentRefunded, InventoryReleased) dinleyerek, sonuç olarak ilgili siparişi iptal eder. Order ID=100 için durum veritabanında “CANCELLED” olarak güncellenir. Böylece Saga, hata ile karşılaştığı için tüm sistemi başlangıç durumuna döndürmüş olur: Müşteriden para çekilmedi (veya iade edildi), stoklar ilk haline getirildi, sipariş kaydı tamamlanmadı (iptal edildi)​
MICROSERVICES.IO
.
Sonlandırma: İptal edilen sipariş ile ilgili bir bilgilendirme gerekebilir; Order Service bu durumu bir olayla dış sistemlere yayabilir veya kullanıcıya hata bildirimi dönebilir. Önemli olan, sistemde kısmen gerçekleşmiş bir işlem kalmamıştır – tutarsız bir durum yoktur. Tüm mikroservisler kendi yaptıkları değişiklikleri geri almıştır ve veritabanları tutarlıdır.
Benzer telafi akışları, diğer ara adımlardaki hatalar için de geçerlidir. Örneğin ödeme adımında hata olsaydı: Payment Service “PaymentFailed” olayı yayınlayacak, bunu duyan Inventory Service hemen stok rezervini iptal edecek (çünkü ödeme gerçekleşmedi) ve Order Service siparişi Cancelled yapacaktır. Bu durumda müşteriden zaten para çekilemediği için ödeme telafisine gerek kalmaz. Yine de sipariş sonuçta iptal edilir. Başka bir senaryoda stok ayırma adımı başarısız olsa (örn. stok yetersiz): Inventory Service “InventoryFailed” olayı yayınlayacak, Order Service bunu duyup siparişi iptal edecektir; Payment Service ise bu olayı duyduğunda belki loglama yapar ama ödemeyi hiç yapmamış olduğundan telafiye gerek yoktur. Tüm bu senaryolarda Saga pattern’inin telafi mekanizması sayesinde sistem tutarlılığı korunur. Test kılavuzu kapsamında, yukarıdaki başarılı ve başarısız senaryolar OpenShift üzerinde yürütülerek gözlemlenecektir. Jaeger üzerinden dağıtık izler incelenip her adımın tetiklenme sırası doğrulanacak, Grafana metrik panellerinden her servis için istek sayıları ve hata oranları takip edilecektir. Kafka konularındaki olayların sırası ve içerikleri kontrol edilerek Outbox pattern’inin düzgün çalıştığı (örn. bir OrderCreated olayı karşılığında en az bir InventoryReserved veya hata olayı görülmesi gibi) teyit edilecektir. Ayrıca, telafi işlemleri sonrasında veritabanı durumlarının (özellikle Payment ve Inventory servislerinde) başlangıç ile tutarlı olduğuna dikkat edilecektir.
Sonuç ve PoC Çıktıları
Bu çalışma sonucunda, dağıtık transaction yönetimi için kurumsal düzeyde tamamlanmış bir PoC ortaya konmuştur. Tüm bileşenleri kapsayan dokümantasyon, mimari kararları ve yapılandırma detaylarını içermektedir. İlgili Kubernetes/OpenShift YAML dosyaları, mikroservisler, Kafka/Debezium altyapısı, Istio servis mesh ve monitoring bileşenleri dahil olmak üzere eksiksiz şekilde hazırlanmıştır. PoC’nin nasıl kurulup çalıştırılacağı ve test edileceğine dair adım adım bir kılavuz sunulmuştur. Örnek transaction akışları ve hata senaryoları ile sistemin ana sistem seviyesinde tutarlılık sağladığı gösterilmiştir. CI/CD boru hattı tanımı sayesinde, bu mimarinin sürekli entegrasyon ve dağıtım süreçlerine uyumlu olduğu kanıtlanmıştır. Sonuç olarak, bu PoC ile mikroservis mimarisinde iki-fazlı commit kullanmadan (XA/2PC yok) dağıtık işlemlerin güvenli bir şekilde yönetilebileceği, event-driven ve eventual consistent yaklaşımla da kurumsal tutarlılık hedeflerinin yakalanabileceği ortaya konmuştur. Bu çözüm, ileride üretim ortamına geçerken ölçekleme, hata toleransı ve izlenebilirlik açısından sağlam bir referans mimari işlevi görecektir. Kaynaklar: Kurumdaki ekiplerin referansı için aşağıda bazı önemli kaynaklar listelenmiştir:
Gunnar Morling, "Saga Orchestration for Microservices Using the Outbox Pattern", InfoQ makalesi – Saga ve Outbox kullanımını anlatan bir örnek​
INFOQ.COM
​
INFOQ.COM
.
Chris Richardson, Microservices.io – Saga Pattern – Saga tasarım deseninin açıklaması ve telafi işlemleri​
MICROSERVICES.IO
.
Debezium Blog, "Reliable Microservices Data Exchange With the Outbox Pattern", Gunnar Morling – Outbox pattern ve Debezium entegrasyonunu anlatan örnek​
DEBEZIUM.IO
​
DEBEZIUM.IO
.
Istio Documentation – Traffic Management ve Fault Injection konuları – Istio ile servisler arası iletişimde güvenlik ve dayanıklılık sağlama​
INFOQ.COM
.
OpenShift Documentation – OpenShift Service Mesh, OpenShift GitOps ve OpenShift Pipelines konuları – Servis mesh ve CI/CD araçlarının OpenShift entegrasyonu​
CODEFRESH.IO
​
CODEFRESH.IO
.
Bu kaynaklar ve PoC kapsamında hazırlanan doküman birlikte incelendiğinde, kurumsal seviyede dağıtık transaction yönetimi için kapsamlı bir rehber elde edilecektir. PoC çıktıları (kod, manifestler, dokümanlar) versiyon kontrol sistemi üzerinde takım ile paylaşılacak ve gerektiğinde güncellenecektir. Böylece bu çalışma, kurumun mikroservis mimarisine geçişinde bir referans uygulama ve öğrenim aracı olarak da değerlendirilebilir.
Şunu dedin:
